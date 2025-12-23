package com.toty.post.dto.convert.postlist;

import com.toty.post.domain.model.post.Post;
import com.toty.post.dto.response.postlist.GeneralPostListResponse;
import com.toty.post.dto.response.postlist.PostListResponse;

public class GeneralPostListResponseStrategy implements PostListResponseStrategy {
    @Override
    public PostListResponse convert(Post post) {
        return new GeneralPostListResponse(
                post.getId(),
                post.getUser().getNickname(),
                post.getUser().getProfileImageUrl(),
                post.getTitle(),
                post.getViewCount(),
                post.getLikeCount(),
                post.getUpdatedAt()
        );
    }
}

