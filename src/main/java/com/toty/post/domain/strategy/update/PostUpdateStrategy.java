package com.toty.post.domain.strategy.update;

import com.toty.post.domain.model.Post;
import com.toty.post.domain.model.PostCategory;
import com.toty.post.dto.request.PostUpdateRequest;

public interface PostUpdateStrategy {
    Post updatePostRequest(PostUpdateRequest postUpdateRequest, Post post);

    PostCategory getPostCategory();
}