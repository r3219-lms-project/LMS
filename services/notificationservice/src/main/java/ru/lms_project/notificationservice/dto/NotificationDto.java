package ru.lms_project.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.lms_project.notificationservice.model.NotificationType;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class NotificationDto {
    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
