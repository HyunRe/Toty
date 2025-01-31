package com.toty.post.application.strategy.convert.postlist;

import com.toty.post.domain.model.Post;
import com.toty.post.presentation.dto.response.postlist.PostListResponse;

public interface PostListResponseStrategy {
    PostListResponse convert(Post post);
}
