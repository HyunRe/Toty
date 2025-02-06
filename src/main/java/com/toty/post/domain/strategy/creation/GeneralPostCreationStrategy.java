package com.toty.post.domain.strategy.creation;

import com.toty.post.application.PostImageService;
import com.toty.post.domain.model.Post;
import com.toty.post.domain.model.PostCategory;
import com.toty.post.dto.request.PostCreateRequest;
import com.toty.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeneralPostCreationStrategy implements PostCreationStrategy {
    private final PostImageService postImageService;

    @Override
    public Post createPostRequest(PostCreateRequest postCreateRequest, User user) {
        Post post = new Post(user, postCreateRequest.getPostCategory(), postCreateRequest.getTitle(),
                postCreateRequest.getContent(), 0, 0, null, null, null);

        // 이미지
        processImages(post, postCreateRequest.getPostImages(), postImageService);

        return post;
    }

    @Override
    public PostCategory getPostCategory() {
        return PostCategory.GENERAL;
    }
}

