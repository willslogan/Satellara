package com.satellaratech.satellara.satellite.scheduler;

import com.satellaratech.satellara.exception.ErrorType;
import com.satellaratech.satellara.exception.SatelliteLocationException;
import com.satellaratech.satellara.utils.SatelliteUtilities;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import java.time.ZonedDateTime;
import java.util.List;

@Component
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

//    @Scheduled(cron = "*/10 * * * * *")
    @Scheduled(cron = "0 1,16,31,46 * * * *")
    public void manageSatelliteLocationPartitions() {
        LocalDateTime currInterval = SatelliteUtilities.currentTimeFlooredForInterval(intervalMinutes);
        // Create the partition for the next interval
        createSatelliteLocationPartition(currInterval);

        // Remove the partition for the previous interval
        purgeSatelliteLocationPartition(currInterval);
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
                jdbcTemplate.execute("DROP TABLE IF EXISTS " + table + " CASCADE");
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
            System.out.println("Tle Data scraped successfully");
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

//    @Scheduled(cron = "*/59 * * * * *")
    @Scheduled(cron = "0 2,17,32,47 * * * *")
    public void calculateITRSCoordinates() {
        System.out.println("Calculating Coordinates now");
        try {
            ProcessBuilder pb = new ProcessBuilder("python3", "./scripts/tleToCartesian.py", String.valueOf(intervalMinutes), String.valueOf(stepSeconds));

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


}
