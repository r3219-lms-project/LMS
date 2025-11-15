package com.lms.courseService.dto;

import com.lms.courseService.model.CourseLevel;
import com.lms.courseService.model.CourseStatus;

public record CreateCourseResponse(
        String id,
        String title,
        String description,
        String thumbnailUrl,
        Integer duration,
        CourseLevel level,
        CourseStatus status
) {}
