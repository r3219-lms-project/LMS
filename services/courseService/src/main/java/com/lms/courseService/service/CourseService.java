package com.lms.courseService.service;

import com.lms.courseService.model.Course;
import com.lms.courseService.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository repo;

    public CourseService(CourseRepository repo) {
        this.repo = repo;
    }

    public Course create(Course course) {
        return repo.save(course);
    }

    public List<Course> getAll() {
        return repo.findAll();
    }

    public Course getById(String id) {
        return repo.findById(id).orElse(null);
    }
}
