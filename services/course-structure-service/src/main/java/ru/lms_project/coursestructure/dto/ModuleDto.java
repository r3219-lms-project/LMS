package ru.lms_project.coursestructure.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModuleDto {
    private String id;
    private String courseId;
    private String title;
    private String description;
    private Integer orderIndex;
    private LocalDateTime createdAt;
    private Long lessonsCount;
}

