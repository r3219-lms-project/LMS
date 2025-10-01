package ru.lms_project.authservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.lms_project.authservice.dto.*;
import ru.lms_project.authservice.exceptions.TokenValidationException;
import ru.lms_project.authservice.model.RefreshTokenSession;
import ru.lms_project.authservice.model.RefreshTokenStatus;
import ru.lms_project.authservice.repository.RefreshTokenSessionRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final TokenService tokenService;
    private final RestTemplate restTemplate;

    @Value("${auth.tokens.refresh.ttl}")
    private Duration refreshTtl;

    @Value("${auth.userservice.base-url}")
    private String userServiceUrl;

    @Override
    public LoginResponse login(LoginRequest request) {
        String url = userServiceUrl + "/users/check-credentials";
        UserAuthCheckResponse responseInfo;

        try {
            responseInfo = restTemplate.postForObject(url, request, UserAuthCheckResponse.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new TokenValidationException("invalid_credentials");
            }
            if (e.getStatusCode() == HttpStatus.FORBIDDEN || e.getStatusCode() == HttpStatus.LOCKED) {
                throw new TokenValidationException("user_inactive");
            }
            throw e;
        }

        if(responseInfo == null) {
            throw new TokenValidationException("user_service_empty_response");
        }

        if (!responseInfo.isValid()) {
            throw new TokenValidationException("bad_credentials");
        }

        UUID userId = responseInfo.getUserId();
        List<String> roles = responseInfo.getRoles() != null ? responseInfo.getRoles() : List.of();

        UUID sessionId = UUID.randomUUID();

        String accessToken = tokenService.generateAccessToken(userId, roles);
        String refreshToken = tokenService.generateRefreshToken(userId, sessionId);

        String tokenHash = hashRefresh(refreshToken);

        RefreshTokenSession refreshTokenSession = new RefreshTokenSession();
        refreshTokenSession.setId(sessionId);
        refreshTokenSession.setUserId(userId);
        refreshTokenSession.setTokenHash(tokenHash);
        refreshTokenSession.setStatus(RefreshTokenStatus.ACTIVE);
        refreshTokenSession.setExpires(Instant.now().plus(refreshTtl));

        refreshTokenSessionRepository.save(refreshTokenSession);

        return new LoginResponse(accessToken, refreshToken);

    }

    private String hashRefresh(String refreshToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new TokenValidationException("hash_error");
        }
    }

    @Override
    @Transactional
    public LoginResponse refreshToken(RefreshRequest oldToken) {

        ParsedRefresh oldTokenParsed = tokenService.parseRefresh(oldToken.getOldRefreshToken());
        RefreshTokenSession rts = refreshTokenSessionRepository.findById(oldTokenParsed.getSessionId())
                .orElseThrow(() -> new TokenValidationException("invalid_sid"));

        if (rts.getExpires().isBefore(Instant.now())) {
            throw new TokenValidationException("expired_refresh");
        }

        if (rts.getStatus() != RefreshTokenStatus.ACTIVE) {
            throw new TokenValidationException("invalid_refresh");
        }

        String tokenHash = hashRefresh(oldToken.getOldRefreshToken());
        if (!tokenHash.equals(rts.getTokenHash())) {
            throw new TokenValidationException("invalid_refresh_hash");
        }

        if(rts.getUserId().equals(oldTokenParsed.getUserId())) {
            throw new TokenValidationException("sid_user_mismatch");
        }

        String url = userServiceUrl + "/users/" + oldTokenParsed.getUserId();
        UserInfoResponse userInfo = restTemplate.getForObject(url, UserInfoResponse.class);
        if (userInfo == null || !userInfo.isActive()) {
            throw new TokenValidationException("user_inactive");
        }

        List<String> roles = userInfo.getRoles();
        int updated = refreshTokenSessionRepository.markActiveAsAlreadyUsed(oldTokenParsed.getSessionId());
        if(updated != 1) {
            throw new TokenValidationException("refresh_reuse_detected");
        }

        UUID newSessionId = UUID.randomUUID();
        String newRefresh = tokenService.generateRefreshToken(oldTokenParsed.getUserId(), newSessionId);
        String newHash = hashRefresh(newRefresh);


        RefreshTokenSession newSession = new RefreshTokenSession();
        newSession.setId(newSessionId);
        newSession.setUserId(oldTokenParsed.getUserId());
        newSession.setTokenHash(newHash);
        newSession.setStatus(RefreshTokenStatus.ACTIVE);
        newSession.setExpires(Instant.now().plus(refreshTtl));
        refreshTokenSessionRepository.save(newSession);

        String newAccess = tokenService.generateAccessToken(oldTokenParsed.getUserId(), roles);

        return new LoginResponse(newAccess, newRefresh);
    }

    @Override
    public void logout(RefreshRequest request) {
        ParsedRefresh oldTokenParsed = tokenService.parseRefresh(request.getOldRefreshToken());
        RefreshTokenSession rts = refreshTokenSessionRepository.findById(oldTokenParsed.getSessionId())
                .orElseThrow(() -> new TokenValidationException("invalid_sid"));

        int expire = refreshTokenSessionRepository.expireIfActive(rts.getId());
        if(expire != 1) {
            throw new TokenValidationException("logout_reuse_detected");
        }
    }

    @Override
    @Transactional
    public void logoutAll(LogoutAllRequest request) {
        refreshTokenSessionRepository.revokeAllActiveByUserId(request.getUserId());
    }
}
