package com.toty.user.presentation.dto.response;

import com.toty.Tag;
import com.toty.user.domain.Site;
import com.toty.user.presentation.dto.LinkDto;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

//본인 정보 확인 시 (json)
@Getter
public class UserInfoResponse {
    private String email; //본인만

    private String nickname;

    private boolean emailSubscribed; //본인만

    private boolean smsSubscribed; //본인만

    private String status_message;

    private String phoneNumber; //본인만

    private List<Tag> tags;

    private String profileImgUrl;

    private List<LinkDto> links; // List<LinkDto>

    private Long followerCount;

    private Long followingCount;


    @Builder
    public UserInfoResponse(String email, String nickname, boolean emailSubscribed,
            boolean smsSubscribed, String status_message, String phoneNumber, List<Tag> tags,
            String profileImgUrl, List<LinkDto> links, Long followerCount, Long followingCount) {
        this.email = email;
        this.nickname = nickname;
        this.emailSubscribed = emailSubscribed;
        this.smsSubscribed = smsSubscribed;
        this.status_message = status_message;
        this.phoneNumber = phoneNumber;
        this.tags = tags;
        this.profileImgUrl = profileImgUrl;
        this.links = links;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
    }
}
