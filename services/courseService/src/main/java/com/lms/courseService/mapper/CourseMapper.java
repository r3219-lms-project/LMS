package com.lms.courseService.mapper;

import com.lms.courseService.dto.CreateCourseRequest;
import com.lms.courseService.dto.CreateCourseResponse;
import com.lms.courseService.model.Course;
import com.lms.courseService.model.CourseStatus;

import java.util.ArrayList;
import java.util.Objects;

public final class CourseMapper {
    private CourseMapper() {}

    public static Course toEntity(CreateCourseRequest req) {
        var c = new Course();
        c.setName(req.name());
        c.setDescription(req.description());
        c.setStudents(req.students() != null ? req.students() : new ArrayList<>());
        c.setDuration(req.duration());
        c.setStatus(Objects.requireNonNullElse(req.status(), CourseStatus.CREATED));
        return c;
    }

    public static CreateCourseResponse toResponse(Course c) {
        return new CreateCourseResponse(
                c.getId(),
                c.getName(),
                c.getDescription(),
                c.getStudents(),
                c.getDuration(),
                c.getStatus()
        );
    }
}
