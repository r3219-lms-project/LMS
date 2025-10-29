package ru.lms_project.common.security;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ParsedToken {
    private UUID userId;
    private List<String> roles;
}
