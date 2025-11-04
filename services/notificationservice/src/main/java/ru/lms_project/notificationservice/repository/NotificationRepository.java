package ru.lms_project.notificationservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ru.lms_project.notificationservice.model.Notification;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Notification> findByUserIdAndReadIsFalseOrderByCreatedAtDesc(UUID userId);

    long countByUserIdAndReadIsFalse(UUID userId);
}
