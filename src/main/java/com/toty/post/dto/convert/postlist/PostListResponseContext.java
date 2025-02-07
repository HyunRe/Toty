package com.toty.post.dto.convert.postlist;

import com.toty.post.domain.model.Post;
import com.toty.post.dto.response.postlist.PostListResponse;

import java.util.List;

public class PostListResponseContext {
    private final PostListResponseStrategy strategy;

    public PostListResponseContext(String postCategory) {
        if ("general".equals(postCategory)) {
            strategy = new GeneralPostListResponseStrategy();
        } else if ("knowledge".equals(postCategory)) {
            strategy = new KnowledgePostListResponseStrategy();
        } else if ("qna".equals(postCategory)) {
            strategy = new QnaPostListResponseStrategy();
        } else {
            strategy = new GeneralPostListResponseStrategy(); // 기본값으로 일반 게시글 전략
        }
    }

    public List<? extends PostListResponse> convertPosts(List<Post> posts) {
        return posts.stream()
                .map(strategy::convert)
                .toList();
    }
}

