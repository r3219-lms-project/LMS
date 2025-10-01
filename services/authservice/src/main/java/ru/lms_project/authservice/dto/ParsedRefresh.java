package ru.lms_project.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ParsedRefresh {
    private UUID userId;
    private UUID sessionId;
    private Instant expiresAt;
    private String jti;
}
