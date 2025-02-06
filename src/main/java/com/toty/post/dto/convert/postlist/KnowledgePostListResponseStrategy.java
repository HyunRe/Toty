package com.toty.post.dto.convert.postlist;

import com.toty.post.domain.model.Post;
import com.toty.post.dto.response.postlist.KnowledgePostListResponse;
import com.toty.post.dto.response.postlist.PostListResponse;

public class KnowledgePostListResponseStrategy implements PostListResponseStrategy {
    @Override
    public PostListResponse convert(Post post) {
        return new KnowledgePostListResponse(
                post.getUser().getNickname(),
                post.getUser().getProfileImageUrl(),
                post.getUser().getRole(), // 멘토 역할
                post.getTitle(),
                post.getViewCount(),
                post.getLikeCount(),
                post.getUpdatedAt()
        );
    }
}

