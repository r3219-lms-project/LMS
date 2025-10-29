package ru.lms_project.common.security.exceptions;

import lombok.Getter;

@Getter
public class TokenValidationException extends RuntimeException {
  private final String code;
  public TokenValidationException(String code) {
    super(code);
    this.code = code;
  }

  public TokenValidationException(String code, String detail) {
    super(detail);
    this.code = code;
  }
}
