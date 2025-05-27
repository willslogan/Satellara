package com.satellaratech.satellara.satellite.controller;

import com.satellaratech.satellara.satellite.model.SatelliteTLE;
import com.satellaratech.satellara.satellite.service.SatelliteTLEService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/satellite/tle")
public class SatelliteTLEController {
    private final SatelliteTLEService satelliteTLEService;

    public SatelliteTLEController(SatelliteTLEService satelliteTLEService) {
        this.satelliteTLEService = satelliteTLEService;
    }

    @PostMapping
    public void addNewSatelliteTLE(@RequestBody SatelliteTLE satelliteTLE) {
        satelliteTLEService.insertNewSatelliteTLE(satelliteTLE);
    }

//    @PostMapping("/generate")
//    public void generateTleData() {
//        satelliteTLEService.generateData();
//    }
}
