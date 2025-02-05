package com.toty.notification.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationReceiveResponse {
    private String senderNickname;
    private String type;
    private String message;
    private String url;
    private boolean isRead = false;

    public NotificationReceiveResponse(String senderNickname, String type, String message, String url, boolean isRead) {
        this.senderNickname = senderNickname;
        this.type = type;
        this.message = message;
        this.url = url;
        this.isRead = isRead;
    }
}
