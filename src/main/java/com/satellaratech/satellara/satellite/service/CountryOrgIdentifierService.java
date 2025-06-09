package com.satellaratech.satellara.satellite.service;

import com.satellaratech.satellara.satellite.model.CountryOrgIdentifier;
import com.satellaratech.satellara.satellite.repository.CountryOrgIdentifierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CountryOrgIdentifierService {
    private final CountryOrgIdentifierRepository countryOrgIdentifierRepository;

    public CountryOrgIdentifierService(CountryOrgIdentifierRepository countryOrgIdentifierRepository) {
        this.countryOrgIdentifierRepository = countryOrgIdentifierRepository;
    }

    public List<CountryOrgIdentifier> getAllCountryOrgs() {
        return countryOrgIdentifierRepository.findAll();
    }

}
