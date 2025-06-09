package com.satellaratech.satellara.satellite.model;

import com.satellaratech.satellara.satellite.model.idclasses.LocationID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

import java.time.ZonedDateTime;

@Entity
@IdClass(LocationID.class)
public class SatelliteLocation {
    @Id
    private Integer norad_id;

    @Id
    private ZonedDateTime time;

    private String name;

    @Column(nullable = false)
    private Double x;

    @Column(nullable = false)
    private Double y;

    @Column(nullable = false)
    private Double z;

    public SatelliteLocation(Integer norad_id, ZonedDateTime time, String name, Double x, Double y, Double z) {
        this.norad_id = norad_id;
        this.time = time;
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public SatelliteLocation() {
    }

    public Integer getNorad_id() {
        return norad_id;
    }

    public void setNorad_id(Integer norad_id) {
        this.norad_id = norad_id;
    }

    public ZonedDateTime getTime() {
        return time;
    }

    public void setTime(ZonedDateTime time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Double getZ() {
        return z;
    }

    public void setZ(Double z) {
        this.z = z;
    }
}
