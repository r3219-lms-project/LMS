package ru.lms_project.common.security;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ParsedToken {
    private UUID userId;
    private List<String> roles;
    private String token;
}
