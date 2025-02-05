package com.toty.notification.presentation.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmTokenRequest {
    private Long userId;
    private String token;

    private FcmTokenRequest(Long userId, String token) {
        this.userId = userId;
        this.token = token;
    }
}
