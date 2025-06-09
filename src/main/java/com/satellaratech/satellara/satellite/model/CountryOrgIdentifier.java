package com.satellaratech.satellara.satellite.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class CountryOrgIdentifier {

    @Id
    private String code;
    private String stateC;
    private String englishName;

    public CountryOrgIdentifier(String code, String stateC, String englishName) {
        this.code = code;
        this.stateC = stateC;
        this.englishName = englishName;
    }

    public CountryOrgIdentifier() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStateC() {
        return stateC;
    }

    public void setStateC(String stateC) {
        this.stateC = stateC;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }
}
