package ru.lms_project.notificationservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.lms_project.notificationservice.model.Notification;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Notification> findByUserIdAndReadIsFalseOrderByCreatedAtDesc(UUID userId);

    long countByUserIdAndReadIsFalse(UUID userId);
}
