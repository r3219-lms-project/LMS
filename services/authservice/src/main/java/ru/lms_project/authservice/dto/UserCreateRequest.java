package ru.lms_project.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserCreateRequest {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;

    private String role;
    private Boolean active;
}
