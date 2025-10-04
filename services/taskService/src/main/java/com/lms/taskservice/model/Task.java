package com.lms.taskservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "tasks")
public class Task {

    @Id
    private String id;

    @NotBlank(message = "Task title cannot be blank")
    @Size(min = 3, max = 100, message = "Task title must be between 3 and 100 characters")
    private String title;

    @NotBlank(message = "Task description cannot be blank")
    @Size(min = 10, max = 1000, message = "Task description must be between 10 and 1000 characters")
    private String description;

    @NotNull(message = "Task type cannot be null")
    private TaskType type;

    @NotNull(message = "Course ID cannot be null")
    private UUID courseId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
