package com.toty.notification.domain.model;

import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash(value = "Notification", timeToLive = 604800)  // 7일 후 만료(TTL 적용)
public class Notification {
    @Id
    private String id;
    private Long receiverId;
    private Long senderId;
    private String senderNickname;

    private String type;        // 알림 타입: LIST, TOAST, PUSH, MESSAGE, EMAIL
    private String message;
    private String url;         // 알림을 클릭 했을 때 이동할 URL

    private boolean isRead = false;
    private LocalDateTime createdAt;

    public Notification(String id, Long receiverId, Long senderId, String senderNickname, String type, String message, String url, boolean isRead, LocalDateTime createdAt) {
        this.id = id;
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.type = type;
        this.message = message;
        this.url = url;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    // 읽음 여부 업데이트
    public void updateIsRead(boolean isRead) {
        this.isRead = isRead;
    }
}
