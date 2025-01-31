package com.toty.post.application.strategy.convert.postlist;

import com.toty.post.domain.model.Post;
import com.toty.post.presentation.dto.response.postlist.PostListResponse;
import com.toty.post.presentation.dto.response.postlist.QnaPostListResponse;

public class QnaPostListResponseStrategy implements PostListResponseStrategy {
    @Override
    public PostListResponse convert(Post post) {
        return new QnaPostListResponse(
                post.getUser().getNickname(),
                post.getUser().getProfileImageUrl(),
                post.getTitle(),
                post.getViewCount(),
                post.getLikeCount(),
                post.getUpdatedAt(),
                post.getPostTags(),
                post.getComments().size()
        );
    }
}

