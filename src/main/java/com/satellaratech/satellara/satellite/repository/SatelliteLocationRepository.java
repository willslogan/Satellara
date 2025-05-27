package com.satellaratech.satellara.satellite.repository;

import com.satellaratech.satellara.satellite.model.SatelliteLocation;
import org.springframework.data.jpa.repository.JpaRepository;


import java.time.ZonedDateTime;
import java.util.List;

public interface SatelliteLocationRepository extends JpaRepository<SatelliteLocation, Integer> {

    List<SatelliteLocation> findSatelliteLocationByTime(ZonedDateTime time);
}
