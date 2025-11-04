package ru.lms_project.notificationservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.lms_project.notificationservice.model.NotificationType;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateNotificationDto {
    @NotNull(message = "User id is required")
    private UUID userId;

    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;

    @NotNull(message = "Title is required")
    private String title;

    private String message;
}
