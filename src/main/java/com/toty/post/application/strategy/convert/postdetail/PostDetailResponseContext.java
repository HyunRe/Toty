package com.toty.post.application.strategy.convert.postdetail;

import com.toty.post.domain.model.Post;
import com.toty.post.presentation.dto.response.postdetail.PostDetailResponse;

public class PostDetailResponseContext {
    private PostDetailResponseStrategy strategy;

    public PostDetailResponseContext(String postCategory) {
        if ("general".equals(postCategory)) {
            strategy = new GeneralPostDetailResponseStrategy();
        } else if ("knowledge".equals(postCategory)) {
            strategy = new KnowledgePostDetailResponseStrategy();
        } else if ("qna".equals(postCategory)) {
            strategy = new QnaPostDetailResponseStrategy();
        } else {
            strategy = new GeneralPostDetailResponseStrategy(); // 기본값으로 일반 게시글 전략
        }
    }

    public PostDetailResponse convertPost(Post post) {
        return strategy.convert(post);
    }
}

