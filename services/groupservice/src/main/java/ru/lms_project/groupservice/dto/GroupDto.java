package ru.lms_project.groupservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupDto {
    private UUID id;
    private String name;
    private String description;
    private UUID teacherId;
    private UUID courseId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;
}

