package com.satellaratech.satellara.satellite.scheduler;

import com.satellaratech.satellara.satellite.model.CountryOrgIdentifier;
import com.satellaratech.satellara.satellite.service.CountryOrgIdentifierService;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SatelliteInfoScheduler {

    private final JdbcTemplate jdbcTemplate;
    private final CountryOrgIdentifierService countryOrgIdentifierService;
    private Map<String, CountryOrgIdentifier> countryOrgIdentifierMap = new HashMap<>();

    private Map<String, String> categories = Map.ofEntries(
            Map.entry("AST", "Astronomy"),
            Map.entry("BIO", "Biology and life sciences"),
            Map.entry("CAL", "Calibration (for atmospheric density, space surveillance radars, ..."),
            Map.entry("COM", "Communications"),
            Map.entry("EDU", "Education purposes"),
            Map.entry("EOSCI", "Earth observing science, except imaging"),
            Map.entry("EW", "Missile early warning, including ballistic missile defence tracking"),
            Map.entry("GEOD", "Geodesy"),
            Map.entry("IMG", "Imaging (optical), except meteorology"),
            Map.entry("IMG-R", "Imaging (radar)"),
            Map.entry("INF", "Infrastructure (support structures, deployers etc)"),
            Map.entry("MET", "Meteorology (imaging)"),
            Map.entry("MET-RO", "Meteorology (radio occultation)"),
            Map.entry("MGRAV", "Microgravity experiments"),
            Map.entry("MISC", "Miscellaneous (e.g. burial services)"),
            Map.entry("NAV", "Navigation, positioning and timing"),
            Map.entry("PLAN", "Deep space related mission"),
            Map.entry("RB", "Rocket body (rocket upper stage)"),
            Map.entry("RV", "Reentry vehicle"),
            Map.entry("SCI", "Scientific studies, except astronomy and Earth observing"),
            Map.entry("SIG", "Signals intelligence"),
            Map.entry("SS", "Spaceship, i.e human spaceflight related (including cargo ships)"),
            Map.entry("TARG", "Target for missile defense or antisatellite tests"),
            Map.entry("TECH", "Technology and training"),
            Map.entry("WEAPON", "Weapon, including antisatellite experiment and FOBS")

    );

    public SatelliteInfoScheduler(JdbcTemplate jdbcTemplate, CountryOrgIdentifierService countryOrgIdentifierService) {
        this.jdbcTemplate = jdbcTemplate;
        this.countryOrgIdentifierService = countryOrgIdentifierService;
        refreshCountryCodes();
    }


    @Scheduled(cron = "0 0 7 * * *")
//    @Scheduled(cron = "*/10 * * * * *")
    public void refreshCountryCodes() {
        String rawCountryCodeTsvData = obtainTsvFile("https://planet4589.org/space/gcat/tsv/tables/orgs.tsv");

        //Data was not received from url
        if (rawCountryCodeTsvData == null)
            return;

        // Create Table Query
        String sqlQuery = "\n" +
                "    create table country_org_identifier (" +
                "        code varchar(255) not null," +
                "        english_name varchar(255)," +
                "        statec varchar(255)," +
                "        primary key (code)" +
                "    )";

        // More efficient to drop table and the recreate table instead of having to run vacuum on truncate
        jdbcTemplate.execute("DROP TABLE IF EXISTS country_org_identifier");
        jdbcTemplate.execute(sqlQuery);

        // Format the tsv file for storage in database
        String formattedTsv = formatCountryCodeTsv(rawCountryCodeTsvData);

        // Copy data to database
        assert jdbcTemplate.getDataSource() != null;
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            CopyManager copyManager = new CopyManager(conn.unwrap(BaseConnection.class));
            copyManager.copyIn("COPY country_org_identifier (code, statec, english_name) FROM STDIN WITH (FORMAT text, DELIMITER E'\\t')", new StringReader(formattedTsv));
            List<CountryOrgIdentifier> allCountryOrgs = countryOrgIdentifierService.getAllCountryOrgs();
            countryOrgIdentifierMap = allCountryOrgs.stream().collect(Collectors.toMap(CountryOrgIdentifier::getCode, countryOrgIdentifier -> countryOrgIdentifier));
            updateSatelliteInfo();
            System.out.println("Success");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public void updateSatelliteInfo() {
        String rawSatelliteInfo = obtainTsvFile("https://planet4589.org/space/gcat/tsv/cat/psatcat.tsv");

        //Data was not received from url
        if (rawSatelliteInfo == null)
            return;

        // Create Table Query
        String sqlQuery =
                "create table satellite_information (" +
                        "        norad_id integer not null," +
                        "        category varchar(255)," +
                        "        country varchar(255)," +
                        "        launchdate varchar(255)," +
                        "        name varchar(255)," +
                        "        primary key (norad_id)" +
                        "    )";

        // More efficient to drop table and the recreate table instead of having to run vacuum on truncate
        jdbcTemplate.execute("DROP TABLE IF EXISTS satellite_information");
        jdbcTemplate.execute(sqlQuery);

        // Format the tsv file for storage in database
        String formattedTsv = formatSatelliteInfo(rawSatelliteInfo);
//        System.out.println(formattedTsv);

        // Copy data to database
        assert jdbcTemplate.getDataSource() != null;
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            CopyManager copyManager = new CopyManager(conn.unwrap(BaseConnection.class));
            copyManager.copyIn("COPY satellite_information (norad_id, name, launchdate, country, category) FROM STDIN WITH (FORMAT text, DELIMITER E'\\t')", new StringReader(formattedTsv));
            System.out.println("Success");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private String obtainTsvFile(String url) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private String formatSatelliteInfo(String tsv) {
        StringBuilder cleanedSatelliteInfo = new StringBuilder();

        // Turn the tsv file into an array of strings to easily access individual rows
        String[] rows = tsv.split("\n");

        // Parse each row
        for (String row : rows) {
            if (row.contains("#") || row.isBlank())
                continue;

            // Separate row further to access individual points of data
            String[] cells = row.split("\t");

            // Obtain needed information

            // Original data had its own nomenclature for norad ids. Removed to stay consistent with current db
            String noradId = cells[0].substring(1);
            String name = cells[2];
            if(noradId.equals("25544")) {
                name = "International Space Station (Zarya)";
            }
            String launchDate = cells[3];
            String countryAffiliation = getCountryAffiliation(cells[17]);

            // Satellites can have multiple categories which is delimited by slashes
            String category = getFullCategories(cells[13]);

            String[] formattedRow = {
                    noradId,
                    name,
                    launchDate,
                    countryAffiliation,
                    category
            };

            cleanedSatelliteInfo.append(String.join("\t", formattedRow)).append("\n");
        }
        return cleanedSatelliteInfo.toString();
    }

    private String getFullCategories(String rawCategories) {
        StringBuilder fullCategories = new StringBuilder();
        String[] rawCatList = rawCategories.split("/");
        for (int i = 0; i < rawCatList.length; i++) {
            // Sometimes categories will contain extra characters (* or ?) for extra information

            // This is the case were category has no extra information
            if(!rawCatList[i].contains("?") && !rawCatList[i].contains("*"))
                fullCategories.append(categories.get(rawCatList[i]));
            else {
                int endIndex;
                // category just has a "*" E.g. MET*
                if (!rawCatList[i].contains("?"))
                    endIndex = rawCatList[i].indexOf("*");
                // category just has a "?" E.g. MET?
                else if (!rawCatList[i].contains("*"))
                    endIndex = rawCatList[i].indexOf("?");
                // category has both E.g. MET?*
                else
                    endIndex = Math.min(rawCatList[i].indexOf("?"), rawCatList[i].indexOf("*"));

                fullCategories.append(categories.get(rawCatList[i].substring(0, endIndex)));
                if(rawCatList[i].contains("?"))
                    fullCategories.append("?");
                if(rawCatList[i].contains("*"))
                    fullCategories.append(" (Orbital data for the satellite was/still is kept secret by the US government)");
            }


            if (i < rawCatList.length-1) {
                fullCategories.append("/");
            }
        }
        return fullCategories.toString();
    }

    private String formatCountryCodeTsv(String tsv) {
        // Format the data for storage
        StringBuilder cleanedCountryCodeTsv = new StringBuilder();
        String[] rows = tsv.split("\n");
        for (String row : rows) {
            if(row.startsWith("#") || row.isBlank())
                continue;

            String[] cells = row.split("\t");
            String[] necessaryCells = {
                    cells[0],
                    cells[2],
                    cells[15]
            };
            cleanedCountryCodeTsv.append(String.join("\t", necessaryCells)).append("\n");
        }
        return cleanedCountryCodeTsv.toString();

    }

    private String getCountryAffiliation(String countryCode) {
        String formattedCountryCode = countryCode.replaceAll("[^A-Za-z-]","");
        CountryOrgIdentifier currOrg = countryOrgIdentifierMap.get(formattedCountryCode);
        if(countryOrgIdentifierMap.isEmpty()) {
            System.out.println("Empty map");
        }
        if (currOrg == null) {
            return "Unknown";
        }

        CountryOrgIdentifier stateOrg = countryOrgIdentifierMap.get(currOrg.getStateC());
        if (stateOrg == null) {
            return "Unknown";
        }
        return stateOrg.getEnglishName();
    }

}
