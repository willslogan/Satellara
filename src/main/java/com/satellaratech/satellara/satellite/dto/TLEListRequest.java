package com.satellaratech.satellara.satellite.dto;

import java.util.List;

public class TLEListRequest {
    private List<Integer> noradIds;

    public List<Integer> getNoradIds() {
        return noradIds;
    }

    public void setNoradIds(List<Integer> noradIds) {
        this.noradIds = noradIds;
    }
}
