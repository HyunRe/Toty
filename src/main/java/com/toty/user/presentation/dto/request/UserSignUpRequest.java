package com.toty.user.presentation.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSignUpRequest {
    private String email;
    private String password;

    public UserSignUpRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
