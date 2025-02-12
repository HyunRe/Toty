package com.toty.user.dto.request;

import com.toty.user.dto.response.LinkDto;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInfoUpdateRequest {
    private String nickname;
    private boolean emailSubscribed;
    private boolean smsSubscribed;
    private boolean notificationAllowed;
    private String statusMessage; // nullable
    private String phoneNumber; // nullable(social)
    private List<String> tags; // nullable
    private List<LinkDto> links; // nullable
}