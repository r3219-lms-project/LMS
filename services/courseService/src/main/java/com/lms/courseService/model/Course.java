package com.lms.courseService.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "courses")
public class Course {

    @Id
    private String id;

    @NotBlank
    private String title;

    private String description;

    private String thumbnailUrl;

    @NotNull
    private Integer duration;

    @NotNull
    private CourseLevel level;

    private CourseStatus status = CourseStatus.CREATED;
}
