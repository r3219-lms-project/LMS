package ru.lms_project.common.security.exceptions;

import lombok.Getter;

@Getter
public class UnauthorizedException extends RuntimeException {
    private final String code;
    public UnauthorizedException(String code) {
        super(code);
        this.code = code;
    }

    public UnauthorizedException(String code, String detail) {
        super(detail);
        this.code = code;
    }
}
