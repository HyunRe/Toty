package com.toty.springconfig.sse;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SseNotificationSendRequest {
    private Long receiverId;
    private Long senderId;
    private String senderNickname;
    private String type;
    private String message;
    private String url;
    private boolean isRead = false;
    private LocalDateTime createdAt;

    // Getter, Setter, Constructor

    public SseNotificationSendRequest(Long receiverId, Long senderId, String senderNickname, String type, String message, String url, boolean isRead, LocalDateTime createdAt) {
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.type = type;
        this.message = message;
        this.url = url;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }
}
