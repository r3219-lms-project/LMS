package com.lms.courseService.dto;

import com.lms.courseService.model.CourseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateCourseRequest(
        @NotBlank String name,
        String description,
        List<String> students,
        @NotNull Integer duration,
        CourseStatus status
) {}
