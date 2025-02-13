package com.toty.post.dto.response.postdetail;

import com.toty.common.pagination.PaginationResult;
import com.toty.post.domain.model.PostCategory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GeneralPostDetailResponse extends PostDetailResponse {
    public GeneralPostDetailResponse(String nickname, String profileImageUrl, PostCategory postCategory, String title, String content,
                                     int viewCount, int likeCount, Boolean isLiked, LocalDateTime earliestTime, PaginationResult comments) {
        super(nickname, profileImageUrl, postCategory, title, content, viewCount, likeCount, isLiked, earliestTime, comments);
    }
}
