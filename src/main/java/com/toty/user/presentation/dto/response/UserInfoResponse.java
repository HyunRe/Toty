package com.toty.user.presentation.dto.response;

import lombok.Getter;

@Getter
public class UserInfoResponse {
    private String email;

    public UserInfoResponse(String email) {
        this.email = email;
    }
}
