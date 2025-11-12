package com.lms.progressService.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonProgressDto {
    private UUID id;
    private UUID userId;
    private UUID lessonId;
    private UUID courseId;
    private Boolean completed;
    private LocalDateTime completedAt;
    private LocalDateTime lastAccessedAt;
}
