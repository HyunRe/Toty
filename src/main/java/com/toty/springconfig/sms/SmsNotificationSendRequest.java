package com.toty.springconfig.sms;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SmsNotificationSendRequest {
    @NotNull(message = "수신자 ID는 필수입니다.")
    private Long receiverId;

    @NotNull(message = "메시지는 필수입니다.")
    @Size(min = 1, max = 25, message = "메시지는 1자 이상, 25 이하이어야 합니다.")
    private String message;

    @NotNull(message = "URL은 필수입니다.")
    @Size(min = 1, max = 100, message = "URL은 1자 이상, 100 이하이어야 합니다.")
    private String url;


    public SmsNotificationSendRequest(Long receiverId, String message, String url) {
        this.receiverId = receiverId;
        this.message = message;
        this.url = url;
    }
}
