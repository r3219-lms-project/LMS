package ru.lms_project.authservice.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class ErrorResponse {
    private final String error;
    private final String message;
    private final Instant timestamp = Instant.now();
}
