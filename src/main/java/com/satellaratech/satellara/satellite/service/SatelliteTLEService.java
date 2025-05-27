package com.satellaratech.satellara.satellite.service;

import com.satellaratech.satellara.exception.ErrorType;
import com.satellaratech.satellara.exception.SatelliteTLEException;
import com.satellaratech.satellara.satellite.model.SatelliteTLE;
import com.satellaratech.satellara.satellite.repository.SatelliteTLERepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.IOException;

@Service
public class SatelliteTLEService {
    private final SatelliteTLERepository satelliteTLERepository;

    public SatelliteTLEService(SatelliteTLERepository satelliteTLERepository) {
        this.satelliteTLERepository = satelliteTLERepository;
    }

    public void insertNewSatelliteTLE(SatelliteTLE satelliteTLE) {
        if(satelliteTLERepository.existsById(satelliteTLE.getNorad_id()))
            throw new SatelliteTLEException(ErrorType.ALREADY_EXISTS, "Satellite TLE already exists for id " + satelliteTLE.getNorad_id());
        satelliteTLERepository.save(satelliteTLE);
    }

//    public void generateData() {
//        satelliteTLERepository.deleteAll();
//        File file = new File("./tledata.txt");
//        System.out.println("Current working directory: " + System.getProperty("user.dir"));
//
//        try (Scanner scanner = new Scanner(file)) {
//            while (scanner.hasNextLine()) {
//                // Skip satellite name line
//                scanner.nextLine();
//
//                // Obtain required TLE fields
//                String tle_s = readNextLineOrThrow(scanner, "Satellite missing tle_s");
//                String tle_t = readNextLineOrThrow(scanner, "Satellite missing tle_t");
//
//                String[] tleParts = tle_t.trim().split("\\s+");
//                if (tleParts.length < 2) {
//                    throw new SatelliteTLEException(ErrorType.MISSING_ARGUMENT, "Invalid TLE format: missing NORAD ID");
//                }
//
//                int norad_id = Integer.parseInt(tleParts[1]);
//                SatelliteTLE satelliteTLE = new SatelliteTLE(tle_s, tle_t, norad_id);
//                insertNewSatelliteTLE(satelliteTLE);
//            }
//        } catch (FileNotFoundException e) {
//            throw new SatelliteTLEException(ErrorType.FILE_NOT_FOUND, "File was not found");
//        }
//    }
//
//    private String readNextLineOrThrow(Scanner scanner, String errorMessage) {
//        if (scanner.hasNextLine()) {
//            return scanner.nextLine();
//        } else {
//            throw new SatelliteTLEException(ErrorType.MISSING_ARGUMENT, errorMessage);
//        }
//    }
}
