package com.satellaratech.satellara.satellite.controller;

import com.satellaratech.satellara.satellite.dto.TLEListRequest;
import com.satellaratech.satellara.satellite.model.SatelliteTLE;
import com.satellaratech.satellara.satellite.service.SatelliteTLEService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/satellite/tle")
public class SatelliteTLEController {
    private final SatelliteTLEService satelliteTLEService;

    public SatelliteTLEController(SatelliteTLEService satelliteTLEService) {
        this.satelliteTLEService = satelliteTLEService;
    }

    @GetMapping("{id}")
    public SatelliteTLE getTleForId(@PathVariable Integer id) {
        return satelliteTLEService.getTleForId(id);
    }

    @PostMapping
    public List<SatelliteTLE> getSatelliteTLEForIds(@RequestBody TLEListRequest tleListRequest) {
        return this.satelliteTLEService.getTleForIds(tleListRequest.getNoradIds());
    }

}
