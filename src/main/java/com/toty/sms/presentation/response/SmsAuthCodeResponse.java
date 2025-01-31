package com.toty.sms.presentation.response;

import com.toty.sms.application.SmsService.PostCategory;
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
