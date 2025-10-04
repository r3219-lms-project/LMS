package ru.lms_project.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UserInfoResponse {
    private UUID id;
    private List<String> roles;
    private boolean isActive;
}
