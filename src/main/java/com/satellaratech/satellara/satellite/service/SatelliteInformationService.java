package com.satellaratech.satellara.satellite.service;

import com.satellaratech.satellara.exception.ErrorType;
import com.satellaratech.satellara.exception.SatelliteInformationException;
import com.satellaratech.satellara.satellite.model.SatelliteInformation;
import com.satellaratech.satellara.satellite.repository.SatelliteInformationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
