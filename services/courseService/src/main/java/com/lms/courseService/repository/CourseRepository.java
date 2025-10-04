package com.lms.courseService.repository;

import com.lms.courseService.model.Course;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CourseRepository extends MongoRepository<Course, String> {
    // Later will be added queries
}
