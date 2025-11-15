package com.lms.courseService.dto;

import com.lms.courseService.model.CourseLevel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCourseRequest(
        @NotBlank String title,
        String description,
        String thumbnailUrl,
        @NotNull @Min(1) Integer duration,
        @NotNull CourseLevel level
) {}
