package com.lms.courseService.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
