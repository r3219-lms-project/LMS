package ru.lms_project.authservice.model;

public enum RefreshTokenStatus {
    ACTIVE,
    ALREADY_USED,
    EXPIRED,
    REVOKED
}
