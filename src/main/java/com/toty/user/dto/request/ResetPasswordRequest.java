package com.toty.user.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResetPasswordRequest {
    private String email;
    private String username;
    private String phoneNumber;
    private String newPassword;
}
