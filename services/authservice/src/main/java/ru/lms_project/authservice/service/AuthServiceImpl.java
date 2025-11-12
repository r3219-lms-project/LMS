package ru.lms_project.authservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.lms_project.authservice.dto.*;
import ru.lms_project.authservice.exceptions.TokenValidationException;
import ru.lms_project.authservice.model.RefreshTokenSession;
import ru.lms_project.authservice.model.RefreshTokenStatus;
import ru.lms_project.authservice.repository.RefreshTokenSessionRepository;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final TokenService tokenService;
    private final RestTemplate restTemplate;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${auth.tokens.refresh.ttl}")
    private Duration refreshTtl;

    @Value("${auth.userservice.base-url}")
    private String userServiceUrl;

    @Override
    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        String raw = request.getPassword() == null ? "" : request.getPassword();

        UserDto user = getUserByEmailOrNull(email);
        if (user == null) throw new TokenValidationException("invalid_credentials");
        if (user.getActive() == null || !user.getActive()) throw new TokenValidationException("user_inactive");

        if (!passwordEncoder.matches(raw, user.getPasswordHash())) {
            throw new TokenValidationException("invalid_credentials");
        }

        List<String> roles = List.of(user.getRole());
        UUID userId = user.getId();
        UUID sessionId = UUID.randomUUID();

        String access = tokenService.generateAccessToken(userId, roles);
        String refresh = tokenService.generateRefreshToken(userId, sessionId);

        String tokenHash = hashRefresh(refresh);
        RefreshTokenSession s = new RefreshTokenSession();
        s.setId(sessionId);
        s.setUserId(userId);
        s.setTokenHash(tokenHash);
        s.setStatus(RefreshTokenStatus.ACTIVE);
        s.setExpires(Instant.now().plus(refreshTtl));
        refreshTokenSessionRepository.save(s);

        return new LoginResponse(access, refresh);
    }

    @Override
    public LoginResponse register(RegisterRequest req) {
        String email = Objects.requireNonNull(req.getEmail(), "email").trim().toLowerCase(Locale.ROOT);
        String password = Objects.requireNonNull(req.getPassword(), "password").trim();
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new TokenValidationException("email_invalid");
        }
        if (password.length() < 8) {
            throw new TokenValidationException("password_weak");
        }


        if (getUserByEmailOrNull(email) != null) {
            throw new TokenValidationException(getUserByEmailOrNull(email).toString());
        }

        String hash = passwordEncoder.encode(password);
        String role = "USER";

        UserCreateRequest create = new UserCreateRequest(
                null,
                req.getFirstName(),
                req.getLastName(),
                email,
                hash,
                role,
                true
        );
        List<String> roles = List.of(role);

        UUID createdId = createUserInUserService(create);
        UUID sessionId = UUID.randomUUID();
        String access = tokenService.generateAccessToken(createdId, roles);
        String refresh = tokenService.generateRefreshToken(createdId, sessionId);
        String tokenHash = hashRefresh(refresh);

        RefreshTokenSession s = new RefreshTokenSession();
        s.setId(sessionId);
        s.setUserId(createdId);
        s.setTokenHash(tokenHash);
        s.setStatus(RefreshTokenStatus.ACTIVE);
        s.setExpires(Instant.now().plus(refreshTtl));
        refreshTokenSessionRepository.save(s);

        return new LoginResponse(access, refresh);
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
        if (!rts.getUserId().equals(oldTokenParsed.getUserId())) {
            throw new TokenValidationException("sid_user_mismatch");
        }

        String url = userServiceUrl + "/" + oldTokenParsed.getUserId();
        UserDto userInfo = restTemplate.getForObject(url, UserDto.class);
        if (userInfo == null || userInfo.getActive() == null || !userInfo.getActive()) {
            throw new TokenValidationException("user_inactive");
        }
        List<String> roles = List.of(userInfo.getRole());

        int updated = refreshTokenSessionRepository.markActiveAsAlreadyUsed(oldTokenParsed.getSessionId());
        if (updated != 1) {
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
        if (expire != 1) {
            throw new TokenValidationException("logout_reuse_detected");
        }
    }

    @Override
    @Transactional
    public void logoutAll(LogoutAllRequest request) {
        refreshTokenSessionRepository.revokeAllActiveByUserId(request.getUserId());
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

    private UserDto getUserByEmailOrNull(String rawEmail) {
        String email = rawEmail == null ? null : rawEmail.trim().toLowerCase(Locale.ROOT);
        var uri = byEmailUri(email);

        try {
            var resp = restTemplate.getForEntity(uri, UserDto.class);

            if (resp.getStatusCode().value() == 404) return null;
            if (!resp.getStatusCode().is2xxSuccessful()) {
                throw new TokenValidationException(resp.toString());
            }

            var dto = resp.getBody();
            if (dto == null || dto.getId() == null || dto.getEmail() == null) return null;
            return dto;

        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            throw new TokenValidationException(e.getMessage());
        }
    }

    private UUID createUserInUserService(UserCreateRequest req) {
        var uri = usersBase();
        try {
            var resp = restTemplate.postForEntity(uri, req, UserCreateResponse.class);
            var body = resp.getBody();
            if (!resp.getStatusCode().is2xxSuccessful() || body == null || body.getId() == null) {
                throw new TokenValidationException("user_create_failed");
            }
            return body.getId();
        } catch (HttpClientErrorException.Conflict e) {
            throw new TokenValidationException("email_already_exists");
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            throw new TokenValidationException(e.getMessage());
        }
    }


    private URI usersBase() {
        return UriComponentsBuilder.fromHttpUrl(userServiceUrl).build(true).toUri();
    }

    private URI byEmailUri(String email) {
        return UriComponentsBuilder.fromHttpUrl(userServiceUrl)
                .path("/by-email")
                .queryParam("email", email)
                .build(true)
                .toUri();
    }

}
