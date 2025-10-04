package com.lms.taskservice.dto;

import com.lms.taskservice.model.TaskType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDto {
    private String id;
    private String title;
    private String description;
    private TaskType type;
    private UUID courseId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
