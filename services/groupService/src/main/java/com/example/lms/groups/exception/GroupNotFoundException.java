package com.example.lms.groups.exception;

public class GroupNotFoundException extends RuntimeException {
  public GroupNotFoundException(String message) {
    super(message);
  }
}
