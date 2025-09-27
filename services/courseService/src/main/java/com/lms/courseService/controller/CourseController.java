package com.lms.courseService.controller;

import com.lms.courseService.model.Course;
import com.lms.courseService.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    // GET /api/v1/courses
    @GetMapping
    public List<Course> getAll() {
        return service.getAll();
    }

    // GET /api/v1/courses/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Course> getById(@PathVariable("id") String id) {
        Course c = service.getById(id);
        return (c == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(c);
    }

    // POST /api/v1/courses
    @PostMapping
    public ResponseEntity<Course> create(@Valid @RequestBody Course request) {
        Course saved = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/courses/" + saved.getId()))
                .body(saved);
    }
}
