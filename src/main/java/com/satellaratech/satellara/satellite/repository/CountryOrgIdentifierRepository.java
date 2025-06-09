package com.satellaratech.satellara.satellite.repository;

import com.satellaratech.satellara.satellite.model.CountryOrgIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryOrgIdentifierRepository extends JpaRepository<CountryOrgIdentifier, String> {
}
