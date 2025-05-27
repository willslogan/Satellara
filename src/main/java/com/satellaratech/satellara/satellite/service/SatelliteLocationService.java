package com.satellaratech.satellara.satellite.service;

import com.satellaratech.satellara.satellite.model.SatelliteLocation;
import com.satellaratech.satellara.satellite.repository.SatelliteLocationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class SatelliteLocationService {
    @Value("${satellite.partition.step.seconds}")
    private int stepSeconds;

    private final SatelliteLocationRepository satelliteLocationRepository;

    public SatelliteLocationService(SatelliteLocationRepository satelliteLocationRepository) {
        this.satelliteLocationRepository = satelliteLocationRepository;
    }

    public List<SatelliteLocation> getCurrentCoordinates() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(now, ZoneOffset.UTC);

        int currSecond = zonedDateTime.getSecond();
        int seconds = (currSecond / stepSeconds) * stepSeconds;
        System.out.println("Searching for coordinates at: " + zonedDateTime.withSecond(seconds));
        return satelliteLocationRepository.findSatelliteLocationByTime(zonedDateTime.withSecond(seconds));
    }

    // more methods here


}
