package com.lms.progressService.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseProgressStatsDto {
    private UUID courseId;
    private long totalLessons;
    private long completedLessons;
    private double completionPercentage;
}
