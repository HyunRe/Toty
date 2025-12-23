package com.toty.post.domain.strategy.creation;

import com.toty.post.domain.model.post.Post;
import com.toty.post.domain.model.post.PostCategory;
import com.toty.post.domain.repository.post.PostRepository;
import com.toty.post.dto.request.PostCreateRequest;
import com.toty.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeneralPostCreationStrategy implements PostCreationStrategy {
    private final PostRepository postRepository;

    @Override
    public Post createPostRequest(PostCreateRequest postCreateRequest, User user) {
        Post post = new Post(user, postCreateRequest.getPostCategory(), postCreateRequest.getTitle(),
                postCreateRequest.getContent(), 0, 0, null, null);

        postRepository.save(post);
        return post;
    }

    @Override
    public PostCategory getPostCategory() {
        return PostCategory.GENERAL;
    }
}
