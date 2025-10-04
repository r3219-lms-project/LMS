package ru.lms_project.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
@Schema(name = "RefreshRequest", description = "Request with old user's refresh token")
public class RefreshRequest {
    @Schema(description = "Old user's refresh token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String oldRefreshToken;
}