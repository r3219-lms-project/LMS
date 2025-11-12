package ru.lms_project.coursestructure.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "lessons")
public class Lesson {

    @Id
    private String id;

    @Indexed
    @NotBlank(message = "Module ID is required")
    private String moduleId;

    @NotBlank(message = "Title is required")
    private String title;

    private String content;

    @NotNull(message = "Lesson type is required")
    private LessonType type;

    private Integer duration; // в минутах

    @NotNull(message = "Order index is required")
    private Integer orderIndex;

    private String videoUrl;

    private LocalDateTime createdAt;

    public Lesson() {
        this.createdAt = LocalDateTime.now();
    }
}
