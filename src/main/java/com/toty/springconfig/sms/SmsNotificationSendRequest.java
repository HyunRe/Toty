package com.toty.springconfig.sms;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SmsNotificationSendRequest {
    private Long receiverId;
    private String message;

    public SmsNotificationSendRequest(Long receiverId, String message) {
        this.receiverId = receiverId;
        this.message = message;
    }
}
