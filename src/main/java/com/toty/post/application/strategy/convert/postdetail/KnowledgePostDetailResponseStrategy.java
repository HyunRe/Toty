package com.toty.post.application.strategy.convert.postdetail;

import com.toty.base.pagination.PaginationResult;
import com.toty.post.domain.model.Post;
import com.toty.post.presentation.dto.response.postdetail.KnowledgePostDetailResponse;
import com.toty.post.presentation.dto.response.postdetail.PostDetailResponse;

public class KnowledgePostDetailResponseStrategy implements PostDetailResponseStrategy {
    @Override
    public PostDetailResponse convert(Post post, PaginationResult pagedComments) {
        return new KnowledgePostDetailResponse(
                post.getUser().getNickname(),
                post.getUser().getProfileImageUrl(),
                // post.getUser().getRole(), // 멘토 역할
                post.getPostCategory(),
                post.getTitle(),
                post.getContent(),
                post.getPostImages(),
                post.getViewCount(),
                post.getLikeCount(),
                post.getUpdatedAt(),
                pagedComments
        );
    }
}
