package ru.lms_project.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ParsedAccess {
    private UUID userId;
    private List<String> roles;
    private Instant expiresAt;
    private String jti;
}
