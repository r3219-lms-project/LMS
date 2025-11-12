package ru.lms_project.coursestructure.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.lms_project.common.security.annotation.RequireAuthenticated;
import ru.lms_project.coursestructure.dto.CourseStructureStatsDto;
import ru.lms_project.coursestructure.service.CourseStructureStatsService;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseStructureStatsController {

    private final CourseStructureStatsService courseStructureStatsService;

    @GetMapping("/{courseId}/structure-stats")
    @RequireAuthenticated
    public ResponseEntity<CourseStructureStatsDto> getCourseStructureStats(
            @PathVariable String courseId
    ) {
        CourseStructureStatsDto stats = courseStructureStatsService.getCourseStructureStats(courseId);
        return ResponseEntity.ok(stats);
    }
}
