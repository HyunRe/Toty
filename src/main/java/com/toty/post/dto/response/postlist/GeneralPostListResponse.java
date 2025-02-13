package com.toty.post.dto.response.postlist;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GeneralPostListResponse extends PostListResponse {
    public GeneralPostListResponse(Long id, String nickname, String profileImageUrl, String title, int viewCount, int likeCount, LocalDateTime earliestTime) {
        super(id, nickname, profileImageUrl, title, viewCount, likeCount, earliestTime);
    }
}
