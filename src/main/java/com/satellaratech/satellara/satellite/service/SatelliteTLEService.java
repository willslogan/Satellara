package com.satellaratech.satellara.satellite.service;

import com.satellaratech.satellara.exception.ErrorType;
import com.satellaratech.satellara.exception.SatelliteTLEException;
import com.satellaratech.satellara.satellite.model.SatelliteTLE;
import com.satellaratech.satellara.satellite.repository.SatelliteTLERepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public SatelliteTLE getTleForId(Integer id) {
        return satelliteTLERepository.findById(id).orElseThrow(() -> new SatelliteTLEException(ErrorType.NOT_FOUND, "Satellite TLE with id " + id + " not found"));
    }

    public List<SatelliteTLE> getAllTles() {
        return satelliteTLERepository.findAll();
    }
}
