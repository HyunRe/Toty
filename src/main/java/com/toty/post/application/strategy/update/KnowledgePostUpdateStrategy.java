package com.toty.post.application.strategy.update;

import com.toty.post.application.PostImageService;
import com.toty.post.domain.model.Post;
import com.toty.post.presentation.dto.request.PostUpdateRequest;
import com.toty.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KnowledgePostUpdateStrategy implements PostUpdateStrategy {
    private final PostImageService postImageService;

    @Override
    public Post updatePostRequest(PostUpdateRequest postUpdateRequest, Post post) {
        Post updatedPost = new Post(post.getUser(), post.getPostCategory(), postUpdateRequest.getTitle(), postUpdateRequest.getContent(),
                post.getViewCount(), post.getLikeCount(), null, null, null);

        // 이미지
        synchronizeImages(updatedPost, postUpdateRequest.getPostImages(), postImageService);

        // 댓글

        return updatedPost;
    }
}
