package com.toty.notification.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_failures")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationFailure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long receiverId;
    private Long senderId;
    private String eventType;
    private String referenceId;
    private int attempts;

    @Column(length = 2000)
    private String lastErrorMessage;

    @Column(length = 4000)
    private String lastErrorStacktrace;

    @Column(length = 4000)
    private String payload;

    private LocalDateTime createdAt;
    private LocalDateTime failedAt;
    private final boolean handled = false;

    public NotificationFailure(Long receiverId, Long senderId, String eventType, String referenceId, int attempts,
                               String lastErrorMessage, String lastErrorStacktrace, String payload) {
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.eventType = eventType;
        this.referenceId = referenceId;
        this.attempts = attempts;
        this.lastErrorMessage = lastErrorMessage;
        this.lastErrorStacktrace = lastErrorStacktrace;
        this.payload = payload;
        this.createdAt = LocalDateTime.now();
        this.failedAt = LocalDateTime.now();
    }
}

