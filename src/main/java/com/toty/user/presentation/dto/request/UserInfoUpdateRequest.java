package com.toty.user.presentation.dto.request;

import com.toty.Tag;
import com.toty.user.presentation.dto.LinkDto;
import java.util.List;
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
    private List<LinkDto> links; // Site, url -> nullable
}