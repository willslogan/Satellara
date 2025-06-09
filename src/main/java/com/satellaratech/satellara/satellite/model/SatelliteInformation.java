package com.satellaratech.satellara.satellite.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class SatelliteInformation {

    @Id
    private Integer norad_id;
    private String name;
    private String launchdate;
    private String country;
    private String category;

    public SatelliteInformation(Integer norad_id, String name, String launchdate, String country, String category) {
        this.norad_id = norad_id;
        this.name = name;
        this.launchdate = launchdate;
        this.country = country;
        this.category = category;
    }

    public SatelliteInformation() {
    }

    public Integer getNorad_id() {
        return norad_id;
    }

    public void setNorad_id(Integer norad_id) {
        this.norad_id = norad_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLaunchdate() {
        return launchdate;
    }

    public void setLaunchdate(String launchdate) {
        this.launchdate = launchdate;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
