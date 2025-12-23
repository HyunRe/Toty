package com.toty.post.dto.convert.postdetail;

import com.toty.common.pagination.PaginationResult;
import com.toty.post.domain.model.post.Post;
import com.toty.post.dto.response.postdetail.KnowledgePostDetailResponse;
import com.toty.post.dto.response.postdetail.PostDetailResponse;

public class KnowledgePostDetailResponseStrategy implements PostDetailResponseStrategy {
    @Override
    public PostDetailResponse convert(Post post, PaginationResult pagedComments, boolean isLiked, boolean isScraped) {
        return new KnowledgePostDetailResponse(
                post.getUser().getId(),
                post.getUser().getNickname(),
                post.getUser().getProfileImageUrl(),
                post.getUser().getRole(), // 멘토 역할
                post.getPostCategory(),
                post.getTitle(),
                post.getContent(),
                post.getViewCount(),
                post.getLikeCount(),
                isLiked,
                isScraped,
                post.getUpdatedAt(),
                pagedComments
        );
    }
}
