package com.toty.post.dto.response.postdetail;

import com.toty.common.pagination.PaginationResult;
import com.toty.post.domain.model.PostCategory;
import com.toty.post.domain.model.PostImage;
import com.toty.user.domain.model.Role;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KnowledgePostDetailResponse extends PostDetailResponse {
    private Role role;

    public KnowledgePostDetailResponse(String nickname, String profileImageUrl, Role role, PostCategory postCategory, String title, String content,
                                       List<PostImage> postImages, int viewCount, int likeCount, LocalDateTime earliestTime, PaginationResult comments) {
        super(nickname, profileImageUrl, postCategory, title, content, postImages, viewCount, likeCount, earliestTime, comments);
        this.role = role;
    }
}
