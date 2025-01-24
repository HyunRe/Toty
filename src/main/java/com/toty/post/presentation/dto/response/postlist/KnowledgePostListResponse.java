package com.toty.post.presentation.dto.response.postlist;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KnowledgePostListResponse extends PostListResponse {
    // private Role role;

    public KnowledgePostListResponse(String nickname, String profileImageUrl, String title, int viewCount, int likeCount, LocalDateTime earliestTime) { // , Role role
        super(nickname, profileImageUrl, title, viewCount, likeCount, earliestTime);
        // this.role = role;
    }
}
