package com.toty.springconfig.email;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailNotificationSendRequest {
    private Long receiverId;
    private String message;

    public EmailNotificationSendRequest(Long receiverId, String message) {
        this.receiverId = receiverId;
        this.message = message;
    }
}
