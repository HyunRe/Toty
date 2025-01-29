package com.toty.user.application.dto.request;

import com.toty.Tag;
import java.util.List;

import com.toty.user.application.dto.response.UserLinkInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 본인 정보 수정 정보(json)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInfoUpdateRequest {
    // nonnull validation?
    private String nickname;
    private boolean emailSubscribed;
    private boolean smsSubscribed;
    private String statusMessage; // nullable
    private String phoneNumber; // nullable(social)
    private List<Tag> tags; // nullable
    private List<UserLinkInfo> links; // Site, url -> nullable
}