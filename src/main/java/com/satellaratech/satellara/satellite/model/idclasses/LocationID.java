package com.satellaratech.satellara.satellite.model.idclasses;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

public class LocationID implements Serializable {
    private int norad_id;

    private ZonedDateTime time;

    public LocationID(int norad_id, ZonedDateTime time) {
        this.norad_id = norad_id;
        this.time = time;
    }

    public LocationID() {
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LocationID that = (LocationID) o;
        return norad_id == that.norad_id && Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(norad_id, time);
    }
}
