package com.lms.courseService.mapper;

import com.lms.courseService.dto.CreateCourseRequest;
import com.lms.courseService.dto.CreateCourseResponse;
import com.lms.courseService.model.Course;

public class CourseMapper {

    public static Course toEntity(CreateCourseRequest req) {
        Course c = new Course();
        c.setTitle(req.title());
        c.setDescription(req.description());
        c.setThumbnailUrl(req.thumbnailUrl());
        c.setDuration(req.duration());
        c.setLevel(req.level());
        return c;
    }

    public static CreateCourseResponse toResponse(Course c) {
        return new CreateCourseResponse(
                c.getId(),
                c.getTitle(),
                c.getDescription(),
                c.getThumbnailUrl(),
                c.getDuration(),
                c.getLevel(),
                c.getStatus()
        );
    }
}
