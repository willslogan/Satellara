package com.satellaratech.satellara.satellite.scheduler;

import com.satellaratech.satellara.satellite.dto.SatelliteInfoDTO;
import com.satellaratech.satellara.satellite.model.CountryOrgIdentifier;
import com.satellaratech.satellara.satellite.model.SatelliteInformation;
import com.satellaratech.satellara.satellite.model.SatelliteTLE;
import com.satellaratech.satellara.satellite.service.CountryOrgIdentifierService;
import com.satellaratech.satellara.satellite.service.SatelliteInformationService;
import com.satellaratech.satellara.satellite.service.SatelliteTLEService;
import jakarta.annotation.PostConstruct;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static com.satellaratech.satellara.utils.SatelliteUtilities.obtainTsvFile;

@Component
@DependsOn("tableManager")
public class SatelliteInfoScheduler {

    private final JdbcTemplate jdbcTemplate;
    private final CountryOrgIdentifierService countryOrgIdentifierService;
    private final SatelliteTLEService satelliteTLEService;
    private final SatelliteInformationService satelliteInformationService;
    private Map<String, CountryOrgIdentifier> countryOrgIdentifierMap = new HashMap<>();
    private Set<Integer> noradIds;

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

    public SatelliteInfoScheduler(JdbcTemplate jdbcTemplate, CountryOrgIdentifierService countryOrgIdentifierService, SatelliteTLEService satelliteTLEService, SatelliteInformationService satelliteInformationService) {
        this.jdbcTemplate = jdbcTemplate;
        this.countryOrgIdentifierService = countryOrgIdentifierService;
        this.satelliteTLEService = satelliteTLEService;
        this.satelliteInformationService = satelliteInformationService;
    }

    @PostConstruct
    public void init() {
        refreshCountryCodes();
        // Create set of norad ids that we need information for
        List<SatelliteTLE> satelliteTLES = satelliteTLEService.getAllTles();
        noradIds = satelliteTLES.stream().map(SatelliteTLE::getNorad_id).collect(Collectors.toSet());

        uploadInformation();
    }


    @Scheduled(cron = "0 0 7 * * *")
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
            System.out.println("Countries updated: Success");

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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

    @Scheduled(cron = "0 11 1 * * *")
    public void uploadInformation() {
        List<SatelliteInformation> satelliteInformationList = satelliteInformationService.getAllSatelliteInformation();

        // Obtain all tles in the list already
        Set<Integer> noradIdsInList = satelliteInformationList.stream().map(SatelliteInformation::getNorad_id).collect(Collectors.toSet());

        // check and add necessary norad ids
        for (Integer noradId : noradIds) {
            if (!noradIdsInList.contains(noradId)) {
                SatelliteInformation satelliteInformation = new SatelliteInformation();
                satelliteInformation.setNorad_id(noradId);
                satelliteInformationList.add(satelliteInformation);
            }
        }

        // Get information from satcat (launch_date, state, totalmass, length, span, OpOrbit(leo, meo, geo))
        Map<Integer, SatelliteInfoDTO> satcat = getSatellitInfoFromSatCat();

        // Get information from psatcat (launch_date, category, UNState)
        Map<Integer, SatelliteInfoDTO> psatcat = getSatelliteInfoFromPsatCat();

        // Get information from current catalog (launch_date, State, OpOrbit)
        Map<Integer, SatelliteInfoDTO> currentcat = getSatelliteInfoFromCurrentCat();

        // Get information from active catalog (launchDate, UnState, mass, category, OpOrbit
        Map<Integer, SatelliteInfoDTO> activecat = getSatelliteInfoFromActiveCat();

        for (SatelliteInformation satelliteInformation : satelliteInformationList) {
            mergeInfo(satelliteInformation, psatcat.get(satelliteInformation.getNorad_id()));
            mergeInfo(satelliteInformation, currentcat.get(satelliteInformation.getNorad_id()));
            mergeInfo(satelliteInformation, activecat.get(satelliteInformation.getNorad_id()));
            mergeInfo(satelliteInformation, satcat.get(satelliteInformation.getNorad_id()));
        }

        satelliteInformationService.updateAllSatelliteInformation(satelliteInformationList);

    }

    private Map<Integer, SatelliteInfoDTO> getSatellitInfoFromSatCat() {
        String tsv = obtainTsvFile("https://planet4589.org/space/gcat/tsv/cat/satcat.tsv");

        String[] rows = tsv.split("\n");
        Map<Integer, SatelliteInfoDTO> satcatmap = new HashMap<>();
        for (int i = 2; i < rows.length; i++) {
            String[] cells = rows[i].split("\t");
            Integer noradId = Integer.parseInt(cells[0].substring(1));

            // Ignore Satellites not currently orbiting earth
            if(!noradIds.contains(noradId))
                continue;

            // Create SatelliteInfoObject
            SatelliteInfoDTO satelliteInfoDTO = new SatelliteInfoDTO();

            // Populate its fields ---------------------------

            // Set Norad id
            satelliteInfoDTO.setNorad_id(noradId);

            // Set launch date
            satelliteInfoDTO.setLaunch_date(cells[7]);

            // Set State
            String country = getCountryAffiliation(cells[15]);
            satelliteInfoDTO.setCountry(country);

            // Set total mass
            satelliteInfoDTO.setMass(cells[19].trim());

            // Set length
            satelliteInfoDTO.setLength(cells[25].trim());

            // Set span
            satelliteInfoDTO.setSpan(cells[29].trim());

            // Set orbit
            satelliteInfoDTO.setOrbitType(orbitTypeFormatted(cells[39]));

            // -----------------------------------------------

            // Add to the satelliteInfoDTOs map
            satcatmap.put(noradId, satelliteInfoDTO);
        }

        return satcatmap;
    }

    private Map<Integer, SatelliteInfoDTO> getSatelliteInfoFromCurrentCat() {
        String tsv = obtainTsvFile("https://planet4589.org/space/gcat/tsv/derived/currentcat.tsv");
        String[] rows = tsv.split("\n");
        Map<Integer, SatelliteInfoDTO> currentcatmap = new HashMap<>();
        for (int i = 2; i < rows.length; i++) {
            String[] cells = rows[i].split("\t");

            // This catalog has more than just satellites and could be added accidentally if not checking this
            if (cells[0].contains("A"))
                break;

            Integer noradId = Integer.parseInt(cells[0].substring(1));

            // Ignore Satellites not currently orbiting earth
            if(!noradIds.contains(noradId))
                continue;

            // Create SatelliteInfoObject
            SatelliteInfoDTO satelliteInfoDTO = new SatelliteInfoDTO();

            // Populate fields ---------------------------------
            satelliteInfoDTO.setNorad_id(noradId);

            // Launch Date
            satelliteInfoDTO.setLaunch_date(cells[7]);

            // State
            satelliteInfoDTO.setCountry(getCountryAffiliation(cells[10]));

            // OpOrbit
            satelliteInfoDTO.setOrbitType(orbitTypeFormatted(cells[22]));
            // -------------------------------------------------

            currentcatmap.put(noradId, satelliteInfoDTO);
        }
        return currentcatmap;
    }

    private Map<Integer, SatelliteInfoDTO> getSatelliteInfoFromPsatCat() {
        String tsv = obtainTsvFile("https://planet4589.org/space/gcat/tsv/cat/psatcat.tsv");
        String[] rows = tsv.split("\n");
        Map<Integer, SatelliteInfoDTO> psatcatmap = new HashMap<>();
        for (int i = 2; i < rows.length; i++) {
            String[] cells = rows[i].split("\t");
            Integer noradId = Integer.parseInt(cells[0].substring(1));

            // Ignore Satellites not currently orbiting earth
            if(!noradIds.contains(noradId))
                continue;

            // Create SatelliteInfoObject
            SatelliteInfoDTO satelliteInfoDTO = new SatelliteInfoDTO();

            // Populate fields ---------------------------------
            // Set norad id
            satelliteInfoDTO.setNorad_id(noradId);

            // Launch Date
            satelliteInfoDTO.setLaunch_date(cells[3]);

            // Category
            satelliteInfoDTO.setCategory(getFullCategories(cells[13]));

            // State
            satelliteInfoDTO.setCountry(getCountryAffiliation(cells[17]));
            // -------------------------------------------------

            psatcatmap.put(noradId, satelliteInfoDTO);
        }
        return psatcatmap;
    }

    private Map<Integer, SatelliteInfoDTO> getSatelliteInfoFromActiveCat() {
        String tsv = obtainTsvFile("https://planet4589.org/space/gcat/tsv/derived/active.tsv");

        String[] rows = tsv.split("\n");
        Map<Integer, SatelliteInfoDTO> activeCatmap = new HashMap<>();

        for (int i = 2; i < rows.length; i++) {
            String[] cells = rows[i].split("\t");
            Integer noradId = Integer.parseInt(cells[0].substring(1));

            // Ignore Satellites not currently orbiting earth
            if(!noradIds.contains(noradId))
                continue;

            // Create SatelliteInfoObject
            SatelliteInfoDTO satelliteInfoDTO = new SatelliteInfoDTO();

            // Populate its fields ---------------------------

            // Set Norad id
            satelliteInfoDTO.setNorad_id(noradId);

            // Launch Date
            satelliteInfoDTO.setLaunch_date(cells[3]);

            // Country affiliation
            satelliteInfoDTO.setCountry(getCountryAffiliation(cells[7]));

            // Mass
            satelliteInfoDTO.setMass(cells[8].trim());

            // Category
            satelliteInfoDTO.setCategory(getFullCategories(cells[10]));

            // Orbit Type
            satelliteInfoDTO.setOrbitType(orbitTypeFormatted(cells[15]));

            // -----------------------------------------------

            // Add to the satelliteInfoDTOs list
            activeCatmap.put(noradId, satelliteInfoDTO);
        }

        return activeCatmap;
    }

    private String orbitTypeFormatted(String unformattedOrbit) {
        if (unformattedOrbit.contains("LEO"))
            return "LEO";
        else if (unformattedOrbit.contains("MEO"))
            return "MEO";
        else if (unformattedOrbit.contains("GEO"))
            return "GEO";
        return null;
    }

    private void mergeInfo(SatelliteInformation satellite, SatelliteInfoDTO satelliteInfoDTO) {
        // Cases where the satellite isn't in catalog being merged
        if(satelliteInfoDTO == null)
            return;

        if (satellite.getLaunchdate() == null && satelliteInfoDTO.getLaunch_date() != null)
            satellite.setLaunchdate(satelliteInfoDTO.getLaunch_date());
        if (satellite.getCountry() == null && satelliteInfoDTO.getCountry() != null)
            satellite.setCountry(satelliteInfoDTO.getCountry());
        if(satellite.getMass() == null && satelliteInfoDTO.getMass() != null)
            satellite.setMass(satelliteInfoDTO.getMass());
        if(satellite.getLength() == null && satelliteInfoDTO.getLength() != null)
            satellite.setLength(satelliteInfoDTO.getLength());
        if(satellite.getSpan() == null && satelliteInfoDTO.getSpan() != null)
            satellite.setSpan(satelliteInfoDTO.getSpan());
        if(satellite.getOrbitType() == null && satelliteInfoDTO.getOrbitType() != null)
            satellite.setOrbitType(satelliteInfoDTO.getOrbitType());
        if(satellite.getCategory() == null && satelliteInfoDTO.getCategory() != null)
            satellite.setCategory(satelliteInfoDTO.getCategory());
    }

}
