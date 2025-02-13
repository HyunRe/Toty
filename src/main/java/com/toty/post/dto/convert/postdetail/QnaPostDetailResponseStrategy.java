package com.toty.post.dto.convert.postdetail;

import com.toty.common.pagination.PaginationResult;
import com.toty.post.domain.model.Post;
import com.toty.post.dto.response.postdetail.PostDetailResponse;
import com.toty.post.dto.response.postdetail.QnaPostDetailResponse;

public class QnaPostDetailResponseStrategy implements PostDetailResponseStrategy {
    @Override
    public PostDetailResponse convert(Post post, PaginationResult pagedComments) {
        return new QnaPostDetailResponse(
                post.getUser().getNickname(),
                post.getUser().getProfileImageUrl(),
                post.getPostCategory(),
                post.getTitle(),
                post.getContent(),
                post.getViewCount(),
                post.getLikeCount(),
                post.getIsLiked(),
                post.getIsScraped(),
                post.getPostTags(), // 기술 태그
                post.getUpdatedAt(),
                pagedComments
        );
    }
}
