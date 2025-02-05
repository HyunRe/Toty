package com.toty.notification.presentation.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmNotificationSendRequest {
    private String senderNickname;
    private String message;
    private String url;

    public FcmNotificationSendRequest(String senderNickname, String message, String url) {
        this.senderNickname = senderNickname;
        this.message = message;
        this.url = url;
    }
}
