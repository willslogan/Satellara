package com.satellaratech.satellara.satellite.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class SatelliteInformation {

    @Id
    private Integer norad_id;
    private String name;
    private String orbit_type;
    private String country;
    private String owner;
    private String users;
    private String launch_date;
    private String purpose;

    @Column(columnDefinition = "TEXT")
    private String extra_info;

    public SatelliteInformation(Integer norad_id, String name, String orbit_type, String country, String owner, String users, String launch_date, String purpose, String extra_info) {
        this.norad_id = norad_id;
        this.name = name;
        this.orbit_type = orbit_type;
        this.country = country;
        this.owner = owner;
        this.users = users;
        this.launch_date = launch_date;
        this.purpose = purpose;
        this.extra_info = extra_info;
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

    public String getOrbit_type() {
        return orbit_type;
    }

    public void setOrbit_type(String orbit_type) {
        this.orbit_type = orbit_type;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getUsers() {
        return users;
    }

    public void setUsers(String users) {
        this.users = users;
    }

    public String getLaunch_date() {
        return launch_date;
    }

    public void setLaunch_date(String launch_date) {
        this.launch_date = launch_date;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getExtra_info() {
        return extra_info;
    }

    public void setExtra_info(String extra_info) {
        this.extra_info = extra_info;
    }
}
