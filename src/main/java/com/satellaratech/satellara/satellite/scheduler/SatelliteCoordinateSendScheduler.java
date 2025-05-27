package com.satellaratech.satellara.satellite.scheduler;

import com.satellaratech.satellara.satellite.websocket.SatelliteWebSocketHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SatelliteCoordinateSendScheduler {

    private final SatelliteWebSocketHandler satelliteWebSocketHandler;

    public SatelliteCoordinateSendScheduler(SatelliteWebSocketHandler satelliteWebSocketHandler) {
        this.satelliteWebSocketHandler = satelliteWebSocketHandler;
    }

    @Scheduled(cron = "0/5 * * * * *")
    public void sendUpdates() {
        satelliteWebSocketHandler.broadcastCoordinates();
    }
}
