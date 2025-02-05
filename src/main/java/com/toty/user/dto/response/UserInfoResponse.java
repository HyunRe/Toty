package com.toty.user.dto.response;

import java.util.List;

import com.toty.base.domain.model.Tag;
import lombok.Builder;
import lombok.Getter;

//본인 정보 확인 시 (json)
@Getter
public class UserInfoResponse {

    private String email; //본인만

    private String username; //본인만

    private String nickname;

    private boolean emailSubscribed;

    private boolean smsSubscribed;

    private boolean notificationAllowed;

    private String status_message;

    private String phoneNumber; //본인만

    private List<Tag> tags;

    private String profileImgUrl;

    private List<UserLinkInfo> links; // List<LinkDto>

    private Long followerCount;

    private Long followingCount;

    private boolean isFollowing; // 타인 정보 조회만

    @Builder
    public UserInfoResponse(String email, String nickname,
                            boolean emailSubscribed, boolean smsSubscribed,
                            boolean notificationAllowed,
                            String status_message, String phoneNumber, List<Tag> tags,
                            String profileImgUrl, List<UserLinkInfo> links,
                            Long followerCount, Long followingCount, String username, boolean isFollowing) {
        this.email = email;
        this.nickname = nickname;
        this.emailSubscribed = emailSubscribed;
        this.smsSubscribed = smsSubscribed;
        this.notificationAllowed = notificationAllowed;
        this.status_message = status_message;
        this.phoneNumber = phoneNumber;
        this.tags = tags;
        this.profileImgUrl = profileImgUrl;
        this.links = links;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
        this.username = username;
        this.isFollowing = isFollowing;
    }
}
