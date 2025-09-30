package com.lms.courseService.dto;

import com.lms.courseService.model.CourseStatus;

import java.util.List;

public record CreateCourseResponse(
        String id,
        String name,
        String description,
        List<String> students,
        Integer duration,
        CourseStatus status
) {}
