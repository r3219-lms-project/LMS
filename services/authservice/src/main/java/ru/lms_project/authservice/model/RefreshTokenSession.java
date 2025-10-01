package ru.lms_project.authservice.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_token_session")
@Data
@EntityListeners(AuditingEntityListener.class)
public class RefreshTokenSession {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", unique = true, nullable = false)
    private String tokenHash;

    @CreatedDate
    @Column(name = "created_date", nullable = false)
    private Instant createdDate;

    @Column(name = "expires", nullable = false)
    private Instant expires;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RefreshTokenStatus status;
}
