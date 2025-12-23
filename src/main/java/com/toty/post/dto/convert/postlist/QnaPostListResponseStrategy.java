package com.toty.post.dto.convert.postlist;

import com.toty.post.domain.model.post.Post;
import com.toty.post.dto.response.postlist.PostListResponse;
import com.toty.post.dto.response.postlist.QnaPostListResponse;

public class QnaPostListResponseStrategy implements PostListResponseStrategy {
    @Override
    public PostListResponse convert(Post post) {
        return new QnaPostListResponse(
                post.getId(),
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

