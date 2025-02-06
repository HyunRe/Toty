package com.toty.notification.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSendRequest {
    private Long receiverId;
    private Long senderId;
    private String senderNickname;
    private String type;
    private String referenceId;

    public NotificationSendRequest(Long receiverId, Long senderId, String senderNickname, String type, String referenceId) {
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.type = type;
        this.referenceId = referenceId;
    }
}