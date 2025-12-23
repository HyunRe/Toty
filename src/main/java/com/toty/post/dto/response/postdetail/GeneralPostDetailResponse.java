package com.toty.post.dto.response.postdetail;

import com.toty.common.pagination.PaginationResult;
import com.toty.post.domain.model.post.PostCategory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GeneralPostDetailResponse extends PostDetailResponse {
    public GeneralPostDetailResponse(Long authorId, String nickname, String profileImageUrl, PostCategory postCategory, String title, String content,
                                     int viewCount, int likeCount, Boolean isLiked, Boolean isScraped, LocalDateTime earliestTime, PaginationResult comments) {
        super(authorId, nickname, profileImageUrl, postCategory, title, content, viewCount, likeCount, isLiked, isScraped, earliestTime, comments);
    }
}