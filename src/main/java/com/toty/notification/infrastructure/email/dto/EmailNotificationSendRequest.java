package com.toty.notification.infrastructure.email.dto;

import com.toty.notification.domain.type.EventType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailNotificationSendRequest {
    @NotNull(message = "수신자 ID는 필수입니다.")
    private Long receiverId;

    @NotNull(message = "메시지는 필수입니다.")
    @Size(min = 1, max = 25, message = "메시지는 1자 이상, 25 이하이어야 합니다.")
    private String message;

    @NotNull(message = "이벤트 타입은 필수입니다.")
    private EventType eventType;

    public EmailNotificationSendRequest(Long receiverId, String message, EventType eventType) {
        this.receiverId = receiverId;
        this.message = message;
        this.eventType = eventType;
    }
}
