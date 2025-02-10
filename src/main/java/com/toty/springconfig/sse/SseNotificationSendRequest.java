package com.toty.springconfig.sse;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SseNotificationSendRequest {
    @NotNull(message = "수신자 닉네임은 필수입니다.")
    private String senderNickname;

    @NotNull(message = "메시지는 필수입니다.")
    @Size(min = 1, max = 25, message = "메시지는 1자 이상, 25 이하이어야 합니다.")
    private String message;

    @NotNull(message = "URL은 필수입니다.")
    @Size(min = 1, max = 100, message = "URL은 1자 이상, 100 이하이어야 합니다.")
    private String url;

    public SseNotificationSendRequest(String senderNickname, String message, String url) {
        this.senderNickname = senderNickname;
        this.message = message;
        this.url = url;
    }
}
