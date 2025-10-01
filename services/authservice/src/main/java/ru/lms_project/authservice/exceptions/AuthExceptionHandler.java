package ru.lms_project.authservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import ru.lms_project.authservice.dto.ApiError;
import ru.lms_project.authservice.dto.ErrorResponse;

import java.util.Set;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(TokenValidationException.class)
    public ResponseEntity<ApiError> handleToken(TokenValidationException ex) {
        String code = ex.getCode();
        HttpStatus status = mapCodeToStatus(code);
        String detail = ex.getMessage();
        return ResponseEntity.status(status).body(new ApiError(code, detail));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex){
        return ResponseEntity.badRequest().body(new ApiError("bad_request", "Validation failed"));
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ApiError> handleHttpClient(HttpClientErrorException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(new ApiError("upstream_error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("server_error", ex.getMessage()));
    }

    private HttpStatus mapCodeToStatus(String code) {
        if(code == null) return HttpStatus.UNAUTHORIZED;

        Set<String> unauthorized = Set.of(
                "invalid", "expired", "invalid_token", "expired_token",
                "invalid_audience", "invalid_subject", "invalid_jti", "invalid_expiration",
                "invalid_sid", "invalid_status", "sid_user_mismatch",
                "invalid_refresh_hash", "expired_refresh", "refresh_reuse_detected",
                "invalid_credentials", "bad_credentials", "user_inactive", "logout_reuse_detected"
        );

        if(unauthorized.contains(code)) {
            return HttpStatus.UNAUTHORIZED;
        }

        if(code.startsWith("bad_request")) {
            return HttpStatus.BAD_REQUEST;
        }

        return HttpStatus.UNAUTHORIZED;
    }
}
