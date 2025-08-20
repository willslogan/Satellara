package com.satellaratech.satellara.satellite.controller;

import com.satellaratech.satellara.satellite.model.SatelliteLocation;
import com.satellaratech.satellara.satellite.service.SatelliteLocationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/satellite/location")
public class SatelliteLocationController {

    private final SatelliteLocationService satelliteLocationService;

    public SatelliteLocationController(SatelliteLocationService satelliteLocationService) {
        this.satelliteLocationService = satelliteLocationService;
    }

    @GetMapping("/current")
    public List<SatelliteLocation> getSatelliteLocations() {
        return satelliteLocationService.getCurrentCoordinates();
    }
}
