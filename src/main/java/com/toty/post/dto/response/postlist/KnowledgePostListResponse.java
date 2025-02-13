package com.toty.post.dto.response.postlist;

import com.toty.user.domain.model.Role;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KnowledgePostListResponse extends PostListResponse {
    private Role role;

    public KnowledgePostListResponse(Long id, String nickname, String profileImageUrl, Role role, String title, int viewCount, int likeCount, LocalDateTime earliestTime) {
        super(id, nickname, profileImageUrl, title, viewCount, likeCount, earliestTime);
        this.role = role;
    }
}
