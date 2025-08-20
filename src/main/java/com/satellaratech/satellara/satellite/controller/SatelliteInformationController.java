package com.satellaratech.satellara.satellite.controller;

import com.satellaratech.satellara.satellite.model.SatelliteInformation;
import com.satellaratech.satellara.satellite.service.SatelliteInformationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/satellite/information")
public class SatelliteInformationController {

    private final SatelliteInformationService satelliteInformationService;

    public SatelliteInformationController(SatelliteInformationService satelliteInformationService) {
        this.satelliteInformationService = satelliteInformationService;
    }

    @GetMapping("{id}")
    public SatelliteInformation getSatelliteInformation(@PathVariable Integer id) {
        return satelliteInformationService.getSatelliteInformationByNoradId(id);
    }

    @GetMapping("/us")
    public List<Integer> getUsNoradIds() {
        List<SatelliteInformation> satellites = satelliteInformationService.getSatelliteInformationUS();

        return satellites.stream()
                .map(SatelliteInformation::getNorad_id)
                .collect(Collectors.toList());
    }

    @GetMapping("/us/leo")
    public List<Integer> getUsNoradIdsLeo() {
        List<SatelliteInformation> satellites = satelliteInformationService.getSatelliteInformationUSLeo();

        return satellites.stream()
                .map(SatelliteInformation::getNorad_id)
                .collect(Collectors.toList());
    }

    @GetMapping("/us/meo")
    public List<Integer> getUsNoradIdsMeo() {
        List<SatelliteInformation> satellites = satelliteInformationService.getSatelliteInformationUSMeo();

        return satellites.stream()
                .map(SatelliteInformation::getNorad_id)
                .collect(Collectors.toList());
    }

    @GetMapping("/us/geo")
    public List<Integer> getUsNoradIdsGeo() {
        List<SatelliteInformation> satellites = satelliteInformationService.getSatelliteInformationUSGeo();

        return satellites.stream()
                .map(SatelliteInformation::getNorad_id)
                .collect(Collectors.toList());
    }

    @GetMapping("/china")
    public List<Integer> getSatelliteInformationChina() {
        List<SatelliteInformation> satellites = satelliteInformationService.getSatelliteInformationChina();

        return satellites.stream()
                .map(SatelliteInformation::getNorad_id)
                .collect(Collectors.toList());
    }

    @GetMapping("/china/leo")
    public List<Integer> getSatelliteInformationChinaLEO() {
        List<SatelliteInformation> satellites = satelliteInformationService.getSatelliteInformationChinaLeo();

        return satellites.stream()
                .map(SatelliteInformation::getNorad_id)
                .collect(Collectors.toList());
    }

    @GetMapping("/china/meo")
    public List<Integer> getSatelliteInformationChinaMeo() {
        List<SatelliteInformation> satellites = satelliteInformationService.getSatelliteInformationChinaMeo();

        return satellites.stream()
                .map(SatelliteInformation::getNorad_id)
                .collect(Collectors.toList());
    }

    @GetMapping("/china/geo")
    public List<Integer> getSatelliteInformationChinaGeo() {
        List<SatelliteInformation> satellites = satelliteInformationService.getSatelliteInformationChinaGeo();

        return satellites.stream()
                .map(SatelliteInformation::getNorad_id)
                .collect(Collectors.toList());
    }

    @GetMapping("/russia")
    public List<Integer> getSatelliteInformationRussia() {
        List<SatelliteInformation> satellites = satelliteInformationService.getSatelliteInformationRussia();

        return satellites.stream()
                .map(SatelliteInformation::getNorad_id)
                .collect(Collectors.toList());
    }

    @GetMapping("/russia/leo")
    public List<Integer> getSatelliteInformationRussiaLeo() {
        List<SatelliteInformation> satellites = satelliteInformationService.getSatelliteInformationRussiaLeo();

        return satellites.stream()
                .map(SatelliteInformation::getNorad_id)
                .collect(Collectors.toList());
    }

    @GetMapping("/russia/meo")
    public List<Integer> getSatelliteInformationRussiaMeo() {
        List<SatelliteInformation> satellites = satelliteInformationService.getSatelliteInformationRussiaMeo();

        return satellites.stream()
                .map(SatelliteInformation::getNorad_id)
                .collect(Collectors.toList());
    }

    @GetMapping("/russia/geo")
    public List<Integer> getSatelliteInformationRussiaGeo() {
        List<SatelliteInformation> satellites = satelliteInformationService.getSatelliteInformationRussiaGeo();

        return satellites.stream()
                .map(SatelliteInformation::getNorad_id)
                .collect(Collectors.toList());
    }
}
