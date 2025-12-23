package com.toty.common.sse.dto;

import com.toty.notification.domain.type.EventType;
import com.toty.notification.dto.request.NotificationSendRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SseNotificationSendRequest extends NotificationSendRequest {
    // 알림 고유 ID (중복 방지용)
    private String id;

    // 부모 클래스의 필드를 중복 선언하지 않고 추가 필드만 정의
    @NotNull(message = "메시지는 필수입니다.")
    @Size(min = 1, max = 25, message = "메시지는 1자 이상, 25 이하이어야 합니다.")
    private String message;

    @NotNull(message = "URL은 필수입니다.")
    @Size(min = 1, max = 100, message = "URL은 1자 이상, 100 이하이어야 합니다.")
    private String url;

    public SseNotificationSendRequest(String id, Long receiverId, Long senderId, String senderNickname,
                                     EventType eventType, String referenceId,
                                     String message, String url) {
        // 부모 생성자 호출
        super(receiverId, senderId, senderNickname, eventType, referenceId, false);
        this.id = id;
        this.message = message;
        this.url = url;
    }
}
