package com.satellaratech.satellara.satellite.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "satellite_tle")
public class SatelliteTLE {
    @Id
    private Integer norad_id;
    private String tle_s;
    private String tle_t;

    public SatelliteTLE(String tle_s, String tle_t, Integer norad_id) {
        this.tle_s = tle_s;
        this.tle_t = tle_t;
        this.norad_id = norad_id;
    }

    public SatelliteTLE() {
    }

    public Integer getNorad_id() {
        return norad_id;
    }

    public void setNorad_id(Integer norad_id) {
        this.norad_id = norad_id;
    }

    public String getTle_s() {
        return tle_s;
    }

    public void setTle_s(String tle_s) {
        this.tle_s = tle_s;
    }

    public String getTle_t() {
        return tle_t;
    }

    public void setTle_t(String tle_t) {
        this.tle_t = tle_t;
    }
}
