package ru.lms_project.authservice.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.lms_project.authservice.dto.ParsedAccess;
import ru.lms_project.authservice.dto.ParsedRefresh;
import ru.lms_project.authservice.exceptions.TokenValidationException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class TokenService {
    private final SecretKey key;

    private final String issuer;

    private final String audience;

    private final Duration ttl;

    private final Duration refreshTtl;

    public TokenService(
            @Value("${auth.tokens.secret}") String secret,
            @Value("${auth.tokens.issuer}") String issuer,
            @Value("${auth.tokens.audience}") String audience,
            @Value("${auth.tokens.access.ttl}") Duration ttl,
            @Value("${auth.tokens.refresh.ttl}") Duration refreshTtl
    ) {
        this.issuer = issuer;
        this.audience = audience;
        this.ttl = ttl;
        this.refreshTtl = refreshTtl;
        byte[] keyBytes;

        try {
            keyBytes = Base64.getDecoder().decode(secret.trim());
        } catch (Exception e) {
            keyBytes = secret.trim().getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("secret length less than 32 bytes");
        }
        key = Keys.hmacShaKeyFor(keyBytes);

    }

    public String generateAccessToken(UUID userId, List<String> roles) {
        Instant now = Instant.now();
        Instant exp = now.plus(ttl);

        if (roles == null) roles = new ArrayList<>();

        return Jwts.builder()
                .issuer(issuer)
                .audience().add(audience).and().subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .id(UUID.randomUUID().toString())
                .claim("roles", roles)
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(UUID userId, UUID sessionId) {
        Instant now = Instant.now();
        Instant exp = now.plus(refreshTtl);

        if(userId == null) {
            throw new IllegalArgumentException("userId is null");
        }

        if (sessionId == null) {
            throw new IllegalArgumentException("sessionId is null");
        }

        return Jwts.builder()
                .issuer(issuer)
                .audience().add(audience).and().subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .id(UUID.randomUUID().toString())
                .claim("sid", sessionId.toString())
                .signWith(key)
                .compact();
    }

    public ParsedAccess parseAccess(String token) {
        if (token == null) {
            throw new TokenValidationException("invalid_token");
        }
    
        String bearerToken = token.trim();
        if (bearerToken.regionMatches(true, 0, "Bearer ", 0, 7)) {
            bearerToken = bearerToken.substring(7).trim();
        }
    
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .clockSkewSeconds(60)
                    .build()
                    .parseSignedClaims(bearerToken)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new TokenValidationException("expired_token");
        } catch (JwtException e) {
            throw new TokenValidationException("invalid_token");
        }
    
        Object aud = claims.getAudience();
        boolean audienceOk = false;
        if (aud instanceof String) {
            audienceOk = audience.equals(aud);
        } else if (aud instanceof Collection<?>) {
            for (Object x : (Collection<?>) aud) {
                if (audience.equals(String.valueOf(x))) {
                    audienceOk = true;
                    break;
                }
            }
        }
        if (!audienceOk) {
            throw new TokenValidationException("invalid_audience");
        }
    
        UUID userId;
        try {
            userId = UUID.fromString(claims.getSubject());
        } catch (Exception e) {
            throw new TokenValidationException("invalid_subject");
        }
    
        Date expDate = claims.getExpiration();
        if (expDate == null) {
            throw new TokenValidationException("invalid_expiration");
        }
        Instant expiresAt = expDate.toInstant();
    
        String jti = claims.getId();
        if (jti == null || jti.isEmpty()) {
            throw new TokenValidationException("invalid_jti");
        }
    
        Object rawRoles = claims.get("roles");
        List<String> roles = new ArrayList<>();
        if (rawRoles == null) {
            throw new TokenValidationException("missing_roles");
        } else if (rawRoles instanceof List<?>) {
            for (Object item : (List<?>) rawRoles) {
                if (!(item instanceof String)) {
                    throw new TokenValidationException("invalid_roles");
                }
                String s = ((String) item).trim();
                if (!s.isEmpty()) roles.add(s);
            }
        } else if (rawRoles instanceof String) {
            for (String part : ((String) rawRoles).split(",")) {
                String s = part.trim();
                if (!s.isEmpty()) roles.add(s);
            }
        } else {
            throw new TokenValidationException("invalid_roles");
        }
        if (roles.isEmpty()) {
            throw new TokenValidationException("invalid_roles");
        }
    
        return new ParsedAccess(userId, roles, expiresAt, jti);
    }

    public ParsedRefresh parseRefresh(String token) {
        if (token == null) {
            throw new TokenValidationException("invalid_token");
        }

        String bearerToken = token.trim();
        if (bearerToken.regionMatches(true, 0, "Bearer ", 0, 7)) {
            bearerToken = bearerToken.substring(7).trim();
        }

        Claims claims;

        try {
            claims = Jwts.parser()
            .verifyWith(key)
            .requireIssuer(issuer)
            .clockSkewSeconds(60)
            .build()
            .parseSignedClaims(bearerToken)
            .getPayload();
        } catch (ExpiredJwtException e) {
            throw new TokenValidationException("expired_token");
        } catch (JwtException e) {
            throw new TokenValidationException("invalid_token");
        }

        Object aud = claims.getAudience();
        boolean audienceOk = false;
        if (aud instanceof String) {
            audienceOk = audience.equals(aud);
        } else if (aud instanceof Collection<?>) {
            for (Object x : (Collection<?>) aud) {
                if (audience.equals(String.valueOf(x))) {
                    audienceOk = true;
                    break;
                }
            }
        }
        if (!audienceOk) {
            throw new TokenValidationException("invalid_audience");
        }

        UUID userId;
        try {
            userId = UUID.fromString(claims.getSubject());
        } catch (Exception e) {
            throw new TokenValidationException("invalid_subject");
        }

        UUID sessionId;
        try {
            String sid = claims.get("sid", String.class);
            sessionId = UUID.fromString(sid);
        } catch (Exception e) {
            throw new TokenValidationException("invalid_sid");
        }
        
        Date expDate = claims.getExpiration();
        if (expDate == null) {
            throw new TokenValidationException("invalid_expiration");
        }
        Instant expiresAt = expDate.toInstant();
    
        String jti = claims.getId();
        if (jti == null || jti.isEmpty()) {
            throw new TokenValidationException("invalid_jti");
        }

        return new ParsedRefresh(userId, sessionId, expiresAt, jti);
    }
}
