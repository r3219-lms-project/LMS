package ru.lms_project.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
@Schema(name = "ApiError", description = "Api error structure")
public class ApiError {
    @Schema(description = "Code off error", example = "invalid_credentials")
    private final String error;
    @Schema(description = "Humanized error info", example = "Session id not found")
    private final String message;
    @Schema(description = "Moment of taking error", example = "2025-10-01T10:15:30Z")
    private final Instant timestamp = Instant.now();

}
