package com.lms.courseService.controller;

import com.lms.courseService.dto.*;
import com.lms.courseService.mapper.CourseMapper;
import com.lms.courseService.model.Course;
import com.lms.courseService.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.lms_project.common.security.RequireAdmin;

import java.net.URI;

import static com.lms.courseService.mapper.CourseMapper.toResponse;

@Tag(name = "Courses", description = "Endpoints for managing courses")
@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    @Operation(summary = "Get all courses (public, paginated)")
    @GetMapping
    public Page<CreateCourseResponse> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        var pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));
        return service.getAll(pageable).map(CourseMapper::toResponse);
    }

    @Operation(summary = "Course search by title (public)")
    @GetMapping("/search")
    public Page<CreateCourseResponse> search(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        var pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));
        return service.search(query, pageable).map(CourseMapper::toResponse);
    }

    @Operation(summary = "Get a course by ID (public)")
    @GetMapping("/{id}")
    public ResponseEntity<CreateCourseResponse> getById(
            @Parameter(description = "Course ID") @PathVariable("id") String id) {
        Course c = service.getById(id);
        return (c == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(toResponse(c));
    }

    @Operation(summary = "Create a new course (admin only)")
    @PostMapping
    @RequireAdmin
    public ResponseEntity<CreateCourseResponse> create(@Valid @RequestBody CreateCourseRequest request) {
        Course saved = service.create(CourseMapper.toEntity(request));
        return ResponseEntity
                .created(URI.create("/api/v1/courses/" + saved.getId()))
                .body(toResponse(saved));
    }

    @Operation(summary = "Delete a course by ID (admin only)")
    @DeleteMapping("/{id}")
    @RequireAdmin
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        boolean deleted = service.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Update a course (admin only)")
    @PutMapping("/{id}")
    @RequireAdmin
    public ResponseEntity<CreateCourseResponse> update(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateCourseRequest request) {
        Course updated = service.update(id, request);
        return (updated == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(toResponse(updated));
    }

    // если нужен changeStatus – его тоже логично сделать только для админа
}
