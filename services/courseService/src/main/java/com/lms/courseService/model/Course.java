package com.lms.courseService.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Document(collection = "courses")
public class Course {
    @Id
    private String id;

    @NotBlank
    private String name;

    private String description;

    // Using string for now cuz no UserService
    private List<String> students;

    @NotNull
    private Integer duration;

    @NotNull
    private CourseStatus status = CourseStatus.CREATED;

    public Course() { }

    public Course(String id, String name, String description, List<String> students, Integer duration, CourseStatus status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.students = students;
        this.duration = duration;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getStudents() {
        return students;
    }

    public void setStudents(List<String> students) {
        this.students = students;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public CourseStatus getStatus() {
        return status;
    }

    public void setStatus(CourseStatus status) {
        this.status = status;
    }
}
