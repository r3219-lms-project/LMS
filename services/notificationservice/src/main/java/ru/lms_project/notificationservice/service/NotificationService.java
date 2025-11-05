package ru.lms_project.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.lms_project.notificationservice.dto.CreateNotificationDto;
import ru.lms_project.notificationservice.dto.NotificationDto;
import ru.lms_project.notificationservice.exceptions.IncorrectDataException;
import ru.lms_project.notificationservice.model.Notification;
import ru.lms_project.notificationservice.repository.NotificationRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationDto createNotification(CreateNotificationDto createNotificationDto) {
        Notification notification = Notification.builder()
                .userId(createNotificationDto.getUserId())
                .type(createNotificationDto.getNotificationType())
                .title(createNotificationDto.getTitle())
                .message(createNotificationDto.getMessage())
                .read(false)
                .build();
        Notification savedNotification = notificationRepository.save(notification);
        return toDTO(savedNotification);
    }

    public List<NotificationDto> getUserNotifications(UUID userId) {
        List<Notification> notificationsFromDb = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        return notificationsFromDb.stream().map(this::toDTO).toList();
    }

    public List<NotificationDto> getUserUnreadNotifications(UUID userId) {
        List<Notification> notificationsFromDb = notificationRepository.findAllByUserIdAndReadIsFalseOrderByCreatedAtDesc(userId);
        return notificationsFromDb.stream().map(this::toDTO).toList();
    }

    public NotificationDto markAsRead(String notificationId, UUID userId) {
        Notification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IncorrectDataException("Notification not found with id: " + notificationId));

        if (!notif.getUserId().equals(userId)) {
            throw new IncorrectDataException("user_id_incorrect");
        }
        notif.setRead(true);
        notificationRepository.save(notif);
        return toDTO(notif);
    }

    public List<NotificationDto> markAllAsRead(UUID userId) {
        List<Notification> notifications = notificationRepository
                .findAllByUserIdAndReadIsFalseOrderByCreatedAtDesc(userId);

        notifications.forEach(n -> n.setRead(true));

        List<Notification> savedNotifications = notificationRepository.saveAll(notifications);
        return savedNotifications.stream().map(this::toDTO).toList();
    }

    public void deleteNotification(String notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IncorrectDataException("Notification not found with id: " + notificationId));

        if(!notification.getUserId().equals(userId)) {
            throw new IncorrectDataException("User does not have permission to delete this notification");
        }
        notificationRepository.delete(notification);
    }

    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndReadIsFalse(userId);
    }

    private NotificationDto toDTO(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
