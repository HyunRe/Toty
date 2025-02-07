package com.toty.user.dto.request;

import java.util.List;

import com.toty.common.domain.Tag;
import com.toty.user.dto.response.UserLinkInfo;
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
    private List<Tag> tags; // nullable
    private List<UserLinkInfo> links; // nullable
}