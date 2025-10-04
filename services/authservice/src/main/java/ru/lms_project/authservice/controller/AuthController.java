package ru.lms_project.authservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.lms_project.authservice.dto.*;
import ru.lms_project.authservice.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Auth operations: login, refresh, logout")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Validates credentials via userservice, creates a refresh session and returns an access/refresh token pair",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(value = """
                  { "email": "user@example.com", "password": "P@ssw0rd!" }
                """)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Access and refresh tokens issued",
                            content = @Content(schema = @Schema(implementation = LoginResponse.class),
                                    examples = @ExampleObject(value = """
                    { "accessToken": "eyJhbGciOiJI...", "refreshToken": "eyJhbGciOiJI..." }
                  """))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials or inactive user",
                            content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh tokens (rotate refresh)",
            description = "Validates old refresh token, marks it as used, creates new session and returns new access/refresh tokens",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RefreshRequest.class),
                            examples = @ExampleObject(value = """
                  { "oldRefreshToken": "eyJhbGciOiJI..." }
                """)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "New token pair issued",
                            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Expired/invalid/used refresh token",
                            content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody RefreshRequest oldToken) {
        return ResponseEntity.ok(authService.refreshToken(oldToken));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout (invalidate one refresh token)",
            description = "Marks the refresh session as expired and denies its reuse",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RefreshRequest.class),
                            examples = @ExampleObject(value = """
                  { "oldRefreshToken": "eyJhbGciOiJI..." }
                """)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Session expired"),
                    @ApiResponse(responseCode = "401", description = "Invalid token or session not found",
                            content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest token) {
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    @Operation(
            summary = "Logout on all devices",
            description = "Revokes all active refresh sessions for a user",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LogoutAllRequest.class),
                            examples = @ExampleObject(value = """
                  { "userId": "f0b2b9c6-7b56-4a0a-8d7a-3f8c9f8f9a10" }
                """)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "204", description = "All sessions revoked"),
                    @ApiResponse(responseCode = "401", description = "Invalid user / no permissions",
                            content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    public ResponseEntity<Void> logoutAll(@RequestBody LogoutAllRequest request) {
        authService.logoutAll(request);
        return ResponseEntity.noContent().build();
    }
}
