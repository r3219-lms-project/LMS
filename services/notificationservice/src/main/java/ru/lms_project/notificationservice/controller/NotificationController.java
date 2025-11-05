package ru.lms_project.notificationservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.lms_project.common.security.SecurityUtils;
import ru.lms_project.common.security.annotation.RequireAdmin;
import ru.lms_project.common.security.annotation.RequireAuth;
import ru.lms_project.notificationservice.dto.CreateNotificationDto;
import ru.lms_project.notificationservice.dto.NotificationDto;
import ru.lms_project.notificationservice.service.NotificationService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/users/me")
    @RequireAuth
    public ResponseEntity<List<NotificationDto>> getMyNotifications() {
        UUID userId = SecurityUtils.getCurrentUserId();
        List<NotificationDto> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread}")
    @RequireAuth
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications() {
        UUID userId = SecurityUtils.getCurrentUserId();
        List<NotificationDto> notifications = notificationService.getUserUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/count")
    @RequireAuth
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        UUID userId = SecurityUtils.getCurrentUserId();
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PutMapping("/{id}/read")
    @RequireAuth
    public ResponseEntity<NotificationDto> readNotification(@PathVariable String id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        NotificationDto response = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/read-all")
    @RequireAuth
    public ResponseEntity<List<NotificationDto>> readAllNotifications() {
        UUID userId = SecurityUtils.getCurrentUserId();
        List<NotificationDto> notifications = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(notifications);
    }

    @DeleteMapping("/{id}")
    @RequireAuth
    public ResponseEntity<Void> deleteNotification(@PathVariable String id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        notificationService.deleteNotification(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    @RequireAdmin
    public ResponseEntity<NotificationDto> createNotification(@RequestBody CreateNotificationDto createNotificationDto) {
        NotificationDto notificationDto = notificationService.createNotification(createNotificationDto);
        return ResponseEntity.ok(notificationDto);
    }

    @GetMapping("/users/{userId}")
    @RequireAdmin
    public ResponseEntity<List<NotificationDto>> getUserNotifications(@PathVariable UUID userId) {
        List<NotificationDto> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }
}
