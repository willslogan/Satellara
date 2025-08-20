package com.satellaratech.satellara.satellite.service;

import com.satellaratech.satellara.exception.ErrorType;
import com.satellaratech.satellara.exception.SatelliteInformationException;
import com.satellaratech.satellara.satellite.model.SatelliteInformation;
import com.satellaratech.satellara.satellite.repository.SatelliteInformationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SatelliteInformationService {

    private final SatelliteInformationRepository satelliteInformationRepository;

    public SatelliteInformationService(SatelliteInformationRepository satelliteInformationRepository) {
        this.satelliteInformationRepository = satelliteInformationRepository;
    }

    public SatelliteInformation getSatelliteInformationByNoradId(Integer id) {
        return satelliteInformationRepository.findById(id)
                .orElseThrow(() -> new SatelliteInformationException(ErrorType.NOT_FOUND, "Satellite for norad id: " + id + " not found"));
    }

    public List<SatelliteInformation> getAllSatelliteInformation() {
        return satelliteInformationRepository.findAll();
    }

    public void updateAllSatelliteInformation(List<SatelliteInformation> satelliteInformationList) {
        satelliteInformationRepository.saveAll(satelliteInformationList);
    }

    public List<SatelliteInformation> getSatelliteInformationUS() {
        return satelliteInformationRepository.findByCountryContaining("United States of America");
    }

    public List<SatelliteInformation> getSatelliteInformationUSLeo() {
        List<SatelliteInformation> satelliteInformationList = getSatelliteInformationUS();
        return satelliteInformationList.stream()
                .filter(sat -> Objects.equals(sat.getOrbitType(), "LEO"))
                .collect(Collectors.toList());
    }

    public List<SatelliteInformation> getSatelliteInformationUSMeo() {
        List<SatelliteInformation> satelliteInformationList = getSatelliteInformationUS();
        return satelliteInformationList.stream()
                .filter(sat -> Objects.equals(sat.getOrbitType(), "MEO"))
                .collect(Collectors.toList());
    }

    public List<SatelliteInformation> getSatelliteInformationUSGeo() {
        List<SatelliteInformation> satelliteInformationList = getSatelliteInformationUS();
        return satelliteInformationList.stream()
                .filter(sat -> Objects.equals(sat.getOrbitType(), "GEO"))
                .collect(Collectors.toList());
    }

    public List<SatelliteInformation> getSatelliteInformationRussia() {
        return satelliteInformationRepository.findByCountryContaining("Russia");
    }

    public List<SatelliteInformation> getSatelliteInformationRussiaLeo() {
        List<SatelliteInformation> satelliteInformationList = getSatelliteInformationRussia();
        return satelliteInformationList.stream()
                .filter(sat -> Objects.equals(sat.getOrbitType(), "LEO"))
                .collect(Collectors.toList());
    }

    public List<SatelliteInformation> getSatelliteInformationRussiaMeo() {
        List<SatelliteInformation> satelliteInformationList = getSatelliteInformationRussia();
        return satelliteInformationList.stream()
                .filter(sat -> Objects.equals(sat.getOrbitType(), "MEO"))
                .collect(Collectors.toList());
    }

    public List<SatelliteInformation> getSatelliteInformationRussiaGeo() {
        List<SatelliteInformation> satelliteInformationList = getSatelliteInformationRussia();
        return satelliteInformationList.stream()
                .filter(sat -> Objects.equals(sat.getOrbitType(), "GEO"))
                .collect(Collectors.toList());
    }

    public List<SatelliteInformation> getSatelliteInformationChina() {
        return satelliteInformationRepository.findByCountryContaining("China");
    }

    public List<SatelliteInformation> getSatelliteInformationChinaLeo() {
        List<SatelliteInformation> satelliteInformationList = getSatelliteInformationChina();
        return satelliteInformationList.stream()
                .filter(sat -> Objects.equals(sat.getOrbitType(), "LEO"))
                .collect(Collectors.toList());
    }

    public List<SatelliteInformation> getSatelliteInformationChinaMeo() {
        List<SatelliteInformation> satelliteInformationList = getSatelliteInformationChina();
        return satelliteInformationList.stream()
                .filter(sat -> Objects.equals(sat.getOrbitType(), "MEO"))
                .collect(Collectors.toList());
    }

    public List<SatelliteInformation> getSatelliteInformationChinaGeo() {
        List<SatelliteInformation> satelliteInformationList = getSatelliteInformationChina();
        return satelliteInformationList.stream()
                .filter(sat -> Objects.equals(sat.getOrbitType(), "GEO"))
                .collect(Collectors.toList());
    }
}
