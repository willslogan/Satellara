package com.satellaratech.satellara.config;

import com.satellaratech.satellara.satellite.service.SatelliteLocationService;
import com.satellaratech.satellara.satellite.websocket.SatelliteWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Value("${satellite.allowed.origin}")
    private String allowedOrigin;

    private final SatelliteLocationService satelliteLocationService;


    public WebSocketConfig(SatelliteLocationService satelliteLocationService) {
        this.satelliteLocationService = satelliteLocationService;
    }


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new SatelliteWebSocketHandler(satelliteLocationService), "/api/satellite/live")
                .setAllowedOrigins(allowedOrigin); // Or restrict to frontend domain
    }
}
