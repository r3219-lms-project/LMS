package ru.lms_project.common.security.exceptions;

import lombok.Getter;

@Getter
public class JwtTokenException extends RuntimeException {
    private final String code;
    public JwtTokenException(String code) {
        super(code);
        this.code = code;
    }

    public JwtTokenException(String code, String detail) {
        super(detail);
        this.code = code;
    }
}
