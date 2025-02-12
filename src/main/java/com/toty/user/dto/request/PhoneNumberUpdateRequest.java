package com.toty.user.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhoneNumberUpdateRequest {
    private String phoneNumber;

    public String getPhoneNumber() {
        return phoneNumber;
    }

}
