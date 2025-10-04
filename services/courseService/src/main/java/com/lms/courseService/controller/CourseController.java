package com.lms.courseService.controller;

import com.lms.courseService.dto.*;
import com.lms.courseService.mapper.CourseMapper;
import com.lms.courseService.model.Course;
import com.lms.courseService.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static com.lms.courseService.mapper.CourseMapper.toResponse;

@Tag(name = "Courses", description = "Endpoints for managing courses")
@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    @Operation(summary = "Get all courses")
    @GetMapping
    public List<Course> getAll() {
        return service.getAll();
    }

    @Operation(summary = "Get a course by ID")
    @GetMapping("/{id}")
    public ResponseEntity<CreateCourseResponse> getById(
            @Parameter(description = "Course ID") @PathVariable("id") String id) {
        Course c = service.getById(id);
        return (c == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(toResponse(c));
    }

    @Operation(summary = "Create a new course")
    @PostMapping
    public ResponseEntity<CreateCourseResponse> create(@Valid @RequestBody CreateCourseRequest request) {
        Course saved = service.create(CourseMapper.toEntity(request));
        return ResponseEntity
                .created(URI.create("/api/v1/courses/" + saved.getId()))
                .body(toResponse(saved));
    }

    @Operation(summary = "Delete a course by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        boolean deleted = service.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Change the status of a course")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Course> changeStatus(
            @PathVariable("id") String id,
            @Valid @RequestBody ChangeStatusRequest req) {
        Course updated = service.changeStatus(id, req.getStatus());
        return (updated == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

    @Operation(summary = "Update a course")
    @PutMapping("/{id}")
    public ResponseEntity<Course> update(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateCourseRequest request) {
        Course updated = service.update(id, request);
        return (updated == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }
}
