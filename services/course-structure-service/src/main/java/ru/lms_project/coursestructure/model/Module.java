package ru.lms_project.coursestructure.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "modules")
public class Module {

    @Id
    private String id;

    @Indexed
    private String courseId;

    private String title;

    private String description;

    private Integer orderIndex;

    private LocalDateTime createdAt;

    public Module() {
        this.createdAt = LocalDateTime.now();
    }
}

