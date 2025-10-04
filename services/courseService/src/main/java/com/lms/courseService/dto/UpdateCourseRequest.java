package com.lms.courseService.dto;

import com.lms.courseService.model.CourseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class UpdateCourseRequest {
    @NotBlank
    private String name;
    private String description;
    private List<String> students;
    @NotNull
    private Integer duration;
    private CourseStatus courseStatus;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getStudents() { return students; }
    public void setStudents(List<String> students) { this.students = students; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public CourseStatus getStatus() { return courseStatus; }
    public void setStatus(CourseStatus courseStatus) { this.courseStatus = courseStatus; }
}
