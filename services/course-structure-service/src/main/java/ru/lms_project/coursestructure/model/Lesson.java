package ru.lms_project.coursestructure.model;

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
    private String moduleId;

    private String title;

    private String content;

    private LessonType type;

    private Integer duration; // в минутах

    private Integer orderIndex;

    private String videoUrl;

    private LocalDateTime createdAt;

    public Lesson() {
        this.createdAt = LocalDateTime.now();
    }
}
