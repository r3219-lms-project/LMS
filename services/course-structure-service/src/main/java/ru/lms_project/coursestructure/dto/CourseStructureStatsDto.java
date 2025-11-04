package ru.lms_project.coursestructure.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseStructureStatsDto {
    private String courseId;
    private long totalModules;
    private long totalLessons;
    private long totalDurationMinutes;
}

