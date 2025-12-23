package com.toty.post.domain.strategy.update;

import com.toty.post.domain.model.post.Post;
import com.toty.post.domain.model.post.PostCategory;
import com.toty.post.dto.request.PostUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeneralPostUpdateStrategy implements PostUpdateStrategy {
    @Override
    public Post updatePostRequest(PostUpdateRequest postUpdateRequest, Post post) {
        return post.updatePost(postUpdateRequest.getTitle(), postUpdateRequest.getContent(), null);
    }

    @Override
    public PostCategory getPostCategory() {
        return PostCategory.GENERAL;
    }
}
