package com.satellaratech.satellara.utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class SatelliteUtilities {
    public static LocalDateTime currentTimeFlooredForInterval(int intervalMinutes) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(now, ZoneOffset.UTC);

        int minutes = localDateTime.getMinute();
        int flooredMinutes = (minutes / intervalMinutes) * intervalMinutes;
        return localDateTime.withMinute(flooredMinutes).withSecond(0).withNano(0);
    }

    public static String obtainTsvFile(String url) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
