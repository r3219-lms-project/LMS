package com.lms.courseService.repository;

import com.lms.courseService.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CourseRepository extends MongoRepository<Course, String> {

    Page<Course> findByTitleContainingIgnoreCase(String q, Pageable pageable);
}
