package ru.lms_project.authservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        String secret = "super-secret-key-super-secret-key-123";
        String issuer = "lms-auth";
        String audience = "lms-api";
        Duration accessTtl = Duration.ofMinutes(15);
        Duration refreshTtl = Duration.ofDays(14);

        tokenService = new TokenService(secret, issuer, audience, accessTtl, refreshTtl);
    }

    @Test
    void generateAndParseAccess_ok() {
        UUID userId = UUID.randomUUID();
        var roles = List.of("USER", "ADMIN");

        String access = tokenService.generateAccessToken(userId, roles);
        var parsed = tokenService.parseAccess(access);

        assertEquals(userId, parsed.getUserId());
        assertTrue(parsed.getRoles().contains("USER"));
        assertNotNull(parsed.getExpiresAt());
        assertNotNull(parsed.getJti());
    }

    @Test
    void generateAndParseRefresh_ok() {
        UUID userId = UUID.randomUUID();
        UUID sid = UUID.randomUUID();

        String refresh = tokenService.generateRefreshToken(userId, sid);
        var parsed = tokenService.parseRefresh(refresh);

        assertEquals(userId, parsed.getUserId());
        assertEquals(sid, parsed.getSessionId());
        assertNotNull(parsed.getExpiresAt());
        assertNotNull(parsed.getJti());
    }

}
