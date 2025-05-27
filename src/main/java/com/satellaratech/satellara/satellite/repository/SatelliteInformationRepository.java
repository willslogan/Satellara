package com.satellaratech.satellara.satellite.repository;

import com.satellaratech.satellara.satellite.model.SatelliteInformation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SatelliteInformationRepository extends JpaRepository<SatelliteInformation, Integer> {
}
