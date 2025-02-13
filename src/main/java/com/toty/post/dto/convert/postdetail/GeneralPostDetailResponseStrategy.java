package com.toty.post.dto.convert.postdetail;

import com.toty.common.pagination.PaginationResult;
import com.toty.post.domain.model.Post;
import com.toty.post.dto.response.postdetail.GeneralPostDetailResponse;
import com.toty.post.dto.response.postdetail.PostDetailResponse;

public class GeneralPostDetailResponseStrategy implements PostDetailResponseStrategy {
    @Override
    public PostDetailResponse convert(Post post, PaginationResult pagedComments) {
        return new GeneralPostDetailResponse(
                post.getUser().getNickname(),
                post.getUser().getProfileImageUrl(),
                post.getPostCategory(),
                post.getTitle(),
                post.getContent(),
                post.getViewCount(),
                post.getLikeCount(),
                post.getIsLiked(),
                post.getUpdatedAt(),
                pagedComments
        );
    }
}
