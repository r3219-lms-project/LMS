package com.lms.courseService.service;

import com.lms.courseService.dto.UpdateCourseRequest;
import com.lms.courseService.model.Course;
import com.lms.courseService.model.CourseStatus;
import com.lms.courseService.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public boolean delete(String id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }

    public Course changeStatus(String id, CourseStatus status) {
        Optional<Course> existingOpt = repo.findById(id);
        if (existingOpt.isEmpty()) return null;

        Course c = existingOpt.get();
        c.setStatus(status);
        return repo.save(c);
    }

    public Course update(String id, UpdateCourseRequest req) {
        Optional<Course> existingOpt = repo.findById(id);
        if (existingOpt.isEmpty()) return null;

        Course existing = existingOpt.get();
        existing.setName(req.getName());
        existing.setDescription(req.getDescription());
        existing.setStudents(req.getStudents());
        existing.setDuration(req.getDuration());
        existing.setStatus(req.getStatus());
        return repo.save(existing);
    }
}
