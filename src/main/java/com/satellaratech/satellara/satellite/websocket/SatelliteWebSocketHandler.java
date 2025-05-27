package com.satellaratech.satellara.satellite.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.satellaratech.satellara.exception.ErrorType;
import com.satellaratech.satellara.exception.SatelliteLocationException;
import com.satellaratech.satellara.satellite.model.SatelliteLocation;
import com.satellaratech.satellara.satellite.service.SatelliteLocationService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SatelliteWebSocketHandler extends TextWebSocketHandler {
    private static final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ObjectMapper mapper;

    private final SatelliteLocationService satelliteLocationService;

    public SatelliteWebSocketHandler(SatelliteLocationService satelliteLocationService) {
        this.satelliteLocationService = satelliteLocationService;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session){
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status){
        sessions.remove(session);
    }

    public void broadcastCoordinates() {
        List<SatelliteLocation> coordinates = satelliteLocationService.getCurrentCoordinates();
        System.out.println("Sending payload "+ coordinates.size());

        try {
            String payload = mapper.writeValueAsString(coordinates);
            for (WebSocketSession session : sessions) {
                System.out.println("Session found: " + session.toString());
                if (session.isOpen()) {
                    System.out.println("Sending to session: " + session.toString());
                    session.sendMessage(new TextMessage(payload));
                }
            }
            System.out.println("Done sending payload ");

        } catch (JsonProcessingException e) {
            System.out.println("Error while sending payload1" + e.getMessage());
            throw new SatelliteLocationException(ErrorType.MAPPING_ERROR, "Issue with mapping coordinates for websocket sending");
        } catch (IOException e) {
            System.out.println("Error while sending payload2");
            throw new SatelliteLocationException(ErrorType.IO_EXCEPTION, "Issue with sending coordinates through websocket");
        }
    }


}
