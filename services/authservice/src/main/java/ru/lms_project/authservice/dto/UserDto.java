package ru.lms_project.authservice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserDto {
    private UUID id;
    private String email;
    private String passwordHash;
    private String role;
    private Boolean active;
    private String firstName;
    private String lastName;
}
