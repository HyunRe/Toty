package com.toty.sms.presentation.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 인증번호
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SmsRequest {

    private String phoneNumber; // 인증번호, 자유, 정보 / 질문

    public SmsRequest(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

}
