package com.toty.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.toty.user.domain.model.Role;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

//본인 정보 확인 시 (json)
@Getter
public class UserInfoResponse {
    private Long id;

    private String email; //본인만

    private String username; //본인만

    private String nickname;

    private boolean emailSubscribed;

    private boolean smsSubscribed;

    private boolean notificationAllowed;

    private String status_message;

    private String phoneNumber; //본인만

    private List<String> tags;

    private String profileImgUrl;

    private List<LinkDto> links;

    private Long followerCount;

    private Long followingCount;

    @JsonProperty("isFollowing")
    private boolean isFollowing; // 타인 정보 조회만

    private Role role;

    private String createdAt;

    @Builder
    public UserInfoResponse(Long id, String email, String nickname,
                            boolean emailSubscribed, boolean smsSubscribed,
                            boolean notificationAllowed,
                            String status_message, String phoneNumber, List<String> tags,
                            String profileImgUrl, List<LinkDto> links,
                            Long followerCount, Long followingCount, String username, boolean isFollowing,
                            Role role, String createdAt) {
        this.id = id;
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
        this.role = role;
        this.createdAt = createdAt;
    }
}