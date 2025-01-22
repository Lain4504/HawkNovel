package com.backend.utils;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class DateTimeFormatterUtils {
    private final Map<Long, Function<LocalDateTime, String>> strategyMap = new LinkedHashMap<>();

    public DateTimeFormatterUtils() {
        strategyMap.put(60L, this::formatInSeconds);
        strategyMap.put(3600L, this::formatInMinutes);
        strategyMap.put(86400L, this::formatInHours);
        strategyMap.put(Long.MAX_VALUE, this::formatInDays);
    }

    private String formatInSeconds(LocalDateTime dateTime) {
        long elapsedSeconds = ChronoUnit.SECONDS.between(dateTime, LocalDateTime.now());
        return elapsedSeconds + " seconds";
    }

    private String formatInMinutes(LocalDateTime dateTime) {
        long elapsedMinutes = ChronoUnit.MINUTES.between(dateTime, LocalDateTime.now());
        return elapsedMinutes + " minutes";
    }

    private String formatInHours(LocalDateTime dateTime) {
        long elapsedHours = ChronoUnit.HOURS.between(dateTime, LocalDateTime.now());
        return elapsedHours + " hours";
    }

    private String formatInDays(LocalDateTime dateTime) {
        java.time.format.DateTimeFormatter dateTimeFormatter = java.time.format.DateTimeFormatter.ISO_DATE;
        return dateTime.format(dateTimeFormatter);
    }

    public String format(LocalDateTime dateTime) {
        long elapsedSeconds = ChronoUnit.SECONDS.between(dateTime, LocalDateTime.now());
        var strategy = strategyMap.entrySet()
                .stream()
                .filter(entry -> elapsedSeconds < entry.getKey())
                .findFirst()
                .orElseThrow();
        return strategy.getValue().apply(dateTime);
    }
}
