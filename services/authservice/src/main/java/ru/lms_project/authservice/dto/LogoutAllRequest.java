package ru.lms_project.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Schema(name = "LogoutAllRequest", description = "Request with id of user to logout from all devices")
public class LogoutAllRequest {
    @NotBlank(message = "userId required")
    @Schema(description = "User id", example = "f0b2b9c6-7b56-4a0a-8d7a-3f8c9f8f9a10")
    private UUID userId;
}
