package ru.lms_project.coursestructure.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseStructureStatsDto {
    private String courseId;
    private Long totalModules;
    private Long totalLessons;
    private Long totalDuration;
}
