package ru.lms_project.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.lms_project.common.security.exceptions.TokenValidationException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Component
public class JwtTokenProvider {
    private SecretKey secretKey;
    private String issuer;
    private String audience;

    public JwtTokenProvider(
            @Value("${auth.tokens.secret}") String secret,
            @Value("${auth.tokens.issuer}") String issuer,
            @Value("${auth.tokens.audience}") String audience
    ) {
        this.issuer = issuer;
        this.audience = audience;

        byte[] keyBytes;

        try {
            keyBytes = Base64.getDecoder().decode(secret.trim());
        } catch (Exception e) {
            keyBytes = secret.trim().getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("secret length less than 32 bytes");
        }
        secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public ParsedToken parseAccessToken(String token) {
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
                    .verifyWith(secretKey)
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

        return new ParsedToken(userId, roles, bearerToken);
    }
}
