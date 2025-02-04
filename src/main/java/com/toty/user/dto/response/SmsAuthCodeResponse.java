package com.toty.user.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SmsAuthCodeResponse {

    private String authCode; // 인증

    public SmsAuthCodeResponse(String authCode) {
        this.authCode = authCode;
    }

}
