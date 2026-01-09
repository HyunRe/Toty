package com.toty.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.toty.user.domain.model.Role;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

//본인 정보 확인 시 (json)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}