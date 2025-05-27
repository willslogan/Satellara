package com.satellaratech.satellara.satellite.repository;

import com.satellaratech.satellara.satellite.model.SatelliteTLE;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SatelliteTLERepository extends JpaRepository<SatelliteTLE, Integer> {
}
