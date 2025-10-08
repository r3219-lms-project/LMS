package ru.lms_project.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private UUID id;
    private String email;
    private String passwordHash;
    private String role;
    private Boolean active;
    private String firstName;
    private String lastName;
}
