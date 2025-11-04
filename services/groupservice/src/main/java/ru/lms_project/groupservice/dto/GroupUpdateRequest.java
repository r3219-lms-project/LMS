package ru.lms_project.groupservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupUpdateRequest {
    private String name;
    private String description;
    private UUID teacherId;
    private UUID courseId;
    private Boolean active;
}
