package com.toty.user.application.dto.response;

import com.toty.Tag;
import java.util.List;

import com.toty.user.domain.SubscribeInfo;
import lombok.Builder;
import lombok.Getter;

//본인 정보 확인 시 (json)
@Getter
public class UserInfoResponse {

    private String email; //본인만

    private String nickname;

    private SubscribeInfo subscribeInfo; // 본인만

    private String status_message;

    private String phoneNumber; //본인만

    private List<Tag> tags;

    private String profileImgUrl;

    private List<UserLinkInfo> links; // List<LinkDto>

    private Long followerCount;

    private Long followingCount;


    @Builder
    public UserInfoResponse(String email, String nickname, SubscribeInfo subscribeInfo,
                            String status_message, String phoneNumber, List<Tag> tags,
                            String profileImgUrl, List<UserLinkInfo> links,
                            Long followerCount, Long followingCount) {
        this.email = email;
        this.nickname = nickname;
        this.subscribeInfo = subscribeInfo;
        this.status_message = status_message;
        this.phoneNumber = phoneNumber;
        this.tags = tags;
        this.profileImgUrl = profileImgUrl;
        this.links = links;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
    }
}
