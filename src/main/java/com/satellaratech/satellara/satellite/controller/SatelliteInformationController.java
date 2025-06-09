package com.satellaratech.satellara.satellite.controller;

import com.satellaratech.satellara.satellite.model.SatelliteInformation;
import com.satellaratech.satellara.satellite.service.SatelliteInformationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/satellite/information")
public class SatelliteInformationController {

    private final SatelliteInformationService satelliteInformationService;

    public SatelliteInformationController(SatelliteInformationService satelliteInformationService) {
        this.satelliteInformationService = satelliteInformationService;
    }

    @GetMapping("{id}")
    public SatelliteInformation getAllSatelliteInformation(@PathVariable Integer id) {
        return satelliteInformationService.getSatelliteInformationByNoradId(id);
    }



}
