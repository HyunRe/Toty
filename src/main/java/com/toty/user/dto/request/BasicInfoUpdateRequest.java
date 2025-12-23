package com.toty.user.dto.request;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BasicInfoUpdateRequest {
    private String nickname;
    private List<String> subscriptionAllowed;
    private String currentPassword;
    private String newPassword;
}
