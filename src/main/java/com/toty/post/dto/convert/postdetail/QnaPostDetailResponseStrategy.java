package com.toty.post.dto.convert.postdetail;

import com.toty.common.pagination.PaginationResult;
import com.toty.post.domain.model.post.Post;
import com.toty.post.dto.response.postdetail.PostDetailResponse;
import com.toty.post.dto.response.postdetail.QnaPostDetailResponse;

public class QnaPostDetailResponseStrategy implements PostDetailResponseStrategy {
    @Override
    public PostDetailResponse convert(Post post, PaginationResult pagedComments, boolean isLiked, boolean isScraped) {
        return new QnaPostDetailResponse(
                post.getUser().getId(),
                post.getUser().getNickname(),
                post.getUser().getProfileImageUrl(),
                post.getPostCategory(),
                post.getTitle(),
                post.getContent(),
                post.getViewCount(),
                post.getLikeCount(),
                isLiked,
                isScraped,
                post.getPostTags(), // 기술 태그
                post.getUpdatedAt(),
                pagedComments
        );
    }
}