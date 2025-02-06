package com.toty.post.dto.response.postlist;

import com.toty.common.domain.BaseTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class PostListResponse extends BaseTime {
    // 사용자 정보
    private String nickname;
    private String profileImageUrl;

    // 게시글 정보
    private String title;
    private int viewCount;
    private int likeCount;
    private LocalDateTime earliestTime;

    public PostListResponse(String nickname, String profileImageUrl, String title, int viewCount, int likeCount, LocalDateTime earliestTime) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.title = title;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.earliestTime = earliestTime;
    }
}
