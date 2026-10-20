package com.gym.service;

import com.gym.config.GymProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** "Now" in the gym's time zone (the PHP code used date_default_timezone_set). */
@Component
public class GymClock {

    /** Same text formats as the PHP attendance table: 2022-06-02 / 09:15 AM. */
    public static final DateTimeFormatter ATTENDANCE_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter ATTENDANCE_TIME = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    private final GymProperties properties;

    public GymClock(GymProperties properties) {
        this.properties = properties;
    }

    public LocalDateTime now() {
        return LocalDateTime.now(properties.zone());
    }

    public LocalDate today() {
        return LocalDate.now(properties.zone());
    }

    public String todayText() {
        return today().format(ATTENDANCE_DATE);
    }
}
