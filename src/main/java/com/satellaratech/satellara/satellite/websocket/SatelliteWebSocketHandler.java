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
import java.util.*;
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
        if(sessions.isEmpty()) return;
        List<SatelliteLocation> coordinates = satelliteLocationService.getCurrentCoordinates();
        System.out.println("Sending payload "+ coordinates.size());

        try {

            String payload = convertToCZML(coordinates);
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

    public String convertToCZML(List<SatelliteLocation> coordinates) {
        List<Object> czml = new LinkedList<>();
        Map<String, Object> czmlHeader = new HashMap<>();
        czmlHeader.put("id", "document");
        czmlHeader.put("version", "1.0");
        czml.add(czmlHeader);

        for (SatelliteLocation location : coordinates) {
            Map<String, Object> satellite = new HashMap<>();
            satellite.put("id", location.getNorad_id());
            satellite.put("name", location.getName());
            Map<String, Object> position = new HashMap<>();
            position.put("cartesian", new Double[]{location.getX(), location.getY(), location.getZ()});
            satellite.put("position", position);
            Map<String, Object> graphics = new HashMap<>();
            graphics.put("pixelSize", 3);
            satellite.put("point",graphics);
            czml.add(satellite);
        }

        String czmlFileContents;
        try {
            czmlFileContents = mapper.writeValueAsString(czml);
        } catch (JsonProcessingException e) {
            czmlFileContents = "[]";
        }
        return czmlFileContents;

    }


}
