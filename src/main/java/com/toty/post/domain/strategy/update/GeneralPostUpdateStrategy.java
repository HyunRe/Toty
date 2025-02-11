package com.toty.post.domain.strategy.update;

import com.toty.post.application.PostImageService;
import com.toty.post.domain.model.Post;
import com.toty.post.domain.model.PostCategory;
import com.toty.post.domain.repository.PostRepository;
import com.toty.post.dto.request.PostUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeneralPostUpdateStrategy implements PostUpdateStrategy {
    private final PostImageService postImageService;
    private final PostRepository postRepository;

    @Override
    public Post updatePostRequest(PostUpdateRequest postUpdateRequest, Post post) {
        Post updatedPost = new Post(post.getUser(), post.getPostCategory(), postUpdateRequest.getTitle(), postUpdateRequest.getContent(),
                post.getViewCount(), post.getLikeCount(), post.getComments(), null, null);

        // 이미지
        synchronizeImages(updatedPost, postUpdateRequest.getPostImages(), postImageService);

        postRepository.save(updatedPost);
        return updatedPost;
    }

    @Override
    public PostCategory getPostCategory() {
        return PostCategory.GENERAL;
    }
}
