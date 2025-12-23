package com.toty.notification.dto.request;

import com.toty.notification.domain.type.EventType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSendRequest {
    private Long receiverId;
    private Long senderId;
    private String senderNickname;
    private EventType eventType;
    private String referenceId;
    private boolean fromRedis = false;

    public NotificationSendRequest(Long receiverId, Long senderId, String senderNickname, EventType eventType, String referenceId, boolean fromRedis) {
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.eventType = eventType;
        this.referenceId = referenceId;
        this.fromRedis = fromRedis;
    }

    public void withFromRedis(boolean fromRedis) {
        this.fromRedis = fromRedis;
    }

    public NotificationSendRequest toBaseRequest(NotificationSendRequest request) {
        return new NotificationSendRequest(
                request.getReceiverId(),
                request.getSenderId(),
                request.getSenderNickname(),
                request.getEventType(),
                request.getReferenceId(),
                false
        );
    }
}