package com.satellaratech.satellara.satellite.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class SatelliteInformation {

    @Id
    private Integer norad_id;
    private String launchdate;
    private String country;
    private String mass;
    private String length;
    private String span;
    private String orbitType;
    private String category;

    public SatelliteInformation(Integer norad_id, String launchdate, String country, String mass, String length, String span, String orbitType, String category) {
        this.norad_id = norad_id;
        this.launchdate = launchdate;
        this.country = country;
        this.mass = mass;
        this.length = length;
        this.span = span;
        this.orbitType = orbitType;
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

    public String getMass() {
        return mass;
    }

    public void setMass(String mass) {
        this.mass = mass;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getSpan() {
        return span;
    }

    public void setSpan(String span) {
        this.span = span;
    }

    public String getOrbitType() {
        return orbitType;
    }

    public void setOrbitType(String orbitType) {
        this.orbitType = orbitType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
