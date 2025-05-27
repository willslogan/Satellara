package com.satellaratech.satellara.utils;

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
}
