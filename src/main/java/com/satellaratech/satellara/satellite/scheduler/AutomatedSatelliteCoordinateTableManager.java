package com.satellaratech.satellara.satellite.scheduler;

import com.satellaratech.satellara.exception.ErrorType;
import com.satellaratech.satellara.exception.SatelliteLocationException;
import com.satellaratech.satellara.utils.SatelliteUtilities;
import jakarta.annotation.PostConstruct;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import java.time.ZonedDateTime;
import java.util.List;

@Component("tableManager")
public class AutomatedSatelliteCoordinateTableManager {
    private final JdbcTemplate jdbcTemplate;

    @Value("${satellite.partition.interval.minutes}")
    private int intervalMinutes;

    @Value("${satellite.partition.step.seconds}")
    private int stepSeconds;

    @Value("${DB_USERNAME}")
    private String dbUsername;

    @Value("${DB_PASSWORD}")
    private String dbPassword;

    public static final String TLEURI = "https://celestrak.org/NORAD/elements/gp.php?GROUP=active&FORMAT=tle";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy_MMdd_HHmm");

    public AutomatedSatelliteCoordinateTableManager(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        obtainTleData();
        calculateITRSCoordinates(true);
        calculateITRSCoordinates(false);
    }

    private void manageSatelliteLocationPartitions(boolean curr) {
        LocalDateTime currInterval = SatelliteUtilities.currentTimeFlooredForInterval(intervalMinutes);
        // Remove the partition for the previous interval
        purgeSatelliteLocationPartition(currInterval);

        // Create partition creates it for the next fifteen minutes
        // For generating coordinates for the current time this a bandaid fix
        if (curr) {
            currInterval = currInterval.minusMinutes(intervalMinutes);
        }
        // Create the partition for the next interval
        createSatelliteLocationPartition(currInterval);


    }

    private void createSatelliteLocationPartition(LocalDateTime currInterval) {
        LocalDateTime nextIntervalStart = currInterval.plusMinutes(intervalMinutes);
        LocalDateTime nextIntervalEnd = nextIntervalStart.plusMinutes(intervalMinutes);

        ZonedDateTime zonedStart = nextIntervalStart.atZone(ZoneOffset.UTC);
        ZonedDateTime zonedEnd = nextIntervalEnd.atZone(ZoneOffset.UTC);

        // For naming the partition
        String from = zonedStart.toString();
        String to = zonedEnd.toString();
        String partitionName = "satellite_location_" + FORMATTER.format(nextIntervalStart);

        String sqlQuery = String.format("""
            CREATE TABLE IF NOT EXISTS %s
            PARTITION OF satellite_location
            FOR VALUES FROM ('%s') TO ('%s');
        """, partitionName, from, to);

        jdbcTemplate.execute(sqlQuery);

        System.out.println("Satellite Partition for: " + partitionName + " created");

    }

    private void purgeSatelliteLocationPartition(LocalDateTime currInterval) {
        System.out.println("Purging Satellites");
        String cutoff = "satellite_location_" + FORMATTER.format(currInterval);
        System.out.println("Cutoff: " + cutoff);
        // Find all coordinates that are outdated
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT tablename FROM pg_tables " +
                        "WHERE tablename LIKE 'satellite_location_%' " +
                        "AND tablename < ?", String.class, cutoff
        );

        for (String table : tables) {
            try {
                jdbcTemplate.execute("DROP TABLE IF EXISTS " + table);
                System.out.println("Dropped partition: " + table);
            } catch (Exception e) {
                System.err.println("Failed to drop table " + table + ": " + e.getMessage());
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void obtainTleData() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(TLEURI)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Files.writeString(Paths.get("./scripts/tledata.txt"), response.body());
            uploadTleData(response.body());
            System.out.println("Tle Data scraped successfully");
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
    @Scheduled(cron = "0 2,17,32,47 * * * *")
    public void populateSatelliteCoordinateTable() {
        calculateITRSCoordinates(false);
    }

    private void calculateITRSCoordinates(boolean curr) {
        // Create and drop necessary tables
        manageSatelliteLocationPartitions(curr);

        System.out.println("Calculating Coordinates now");
        try {
            ProcessBuilder pb;
            if (curr)
                pb = new ProcessBuilder("python3", "./scripts/tleToCartesian.py", String.valueOf(intervalMinutes), String.valueOf(stepSeconds), "true");
            else
                pb = new ProcessBuilder("python3", "./scripts/tleToCartesian.py", String.valueOf(intervalMinutes), String.valueOf(stepSeconds));

            pb.environment().put("DB_USERNAME", dbUsername);
            pb.environment().put("DB_PASSWORD", dbPassword);
            pb.redirectErrorStream(true);

            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[PYTHON OUTPUT] " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new SatelliteLocationException(ErrorType.SCRIPT_RUNTIME_ERROR, "Script Status code was non-zero: " + exitCode);
            }
        } catch (IOException e) {
            throw new SatelliteLocationException(ErrorType.SCRIPT_RUNTIME_ERROR, "IOException occurred while generating Satellite Location");
        } catch (InterruptedException e) {
            throw new SatelliteLocationException(ErrorType.SCRIPT_RUNTIME_ERROR, "InterruptedException occurred while generating Satellite Location");
        } finally {
            System.out.println("Satellite database populator finished");
        }
    }

    private void uploadTleData(String tleData) {
        // Obtain a cleaned version of tle data
        String cleanedTleData = formatTleData(tleData);

        // Get rid of old tle data and recreate table
        String sqlQuery = "create table satellite_tle (norad_id integer not null, tle_s varchar(255), tle_t varchar(255), primary key (norad_id))";
        jdbcTemplate.execute("DROP TABLE IF EXISTS satellite_tle");
        jdbcTemplate.execute(sqlQuery);

        // Place new tle data in the table
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            CopyManager copyManager = new CopyManager(conn.unwrap(BaseConnection.class));

            // Convert CSV string to input stram
            InputStream tleStream = new ByteArrayInputStream(cleanedTleData.getBytes(StandardCharsets.UTF_8));

            // Update table with new tle information
            copyManager.copyIn("COPY satellite_tle (norad_id, tle_s, tle_t) FROM STDIN WITH (FORMAT csv)", tleStream);

            System.out.println("Satellite tle copied successfully");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    private String formatTleData(String tleData) {
        StringBuilder sb = new StringBuilder();
        String[] lines = tleData.split("\n");
        for (int i = 0; i < lines.length; i += 3) {
            String name = lines[i];
            String line1 = lines[i + 1].trim();
            String line2 = lines[i + 2].trim();
            String noradId = line2.split("\\s+")[1];

            // what?

            sb.append(String.join(",", noradId, line1, line2));
            sb.append("\n");
        }
        return sb.toString();
    }

}
