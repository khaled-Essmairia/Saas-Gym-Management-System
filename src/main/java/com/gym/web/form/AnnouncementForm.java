package com.gym.web.form;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Announcement form (announcement.php). */
public class AnnouncementForm {

    @NotBlank(message = "Please enter a message")
    @Size(max = 100, message = "Message: max 100 characters")
    private String message;

    @NotNull(message = "Please choose a date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
