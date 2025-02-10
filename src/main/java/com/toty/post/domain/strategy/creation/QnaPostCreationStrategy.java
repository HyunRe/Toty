package com.toty.post.domain.strategy.creation;

import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.post.application.PostImageService;
import com.toty.post.domain.model.Post;
import com.toty.post.domain.model.PostCategory;
import com.toty.post.domain.model.PostTag;
import com.toty.post.domain.repository.PostRepository;
import com.toty.post.domain.repository.PostTagRepository;
import com.toty.post.dto.request.PostCreateRequest;
import com.toty.user.domain.model.User;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class QnaPostCreationStrategy implements PostCreationStrategy {
    private final PostImageService postImageService;
    private final PostTagRepository postTagRepository;
    private final PostRepository postRepository;

    @Override
    public Post createPostRequest(PostCreateRequest postCreateRequest, User user) {
        // 태그 검증
        List<PostTag> postTags = postCreateRequest.getPostTags();
        Optional.ofNullable(postTags)
                .filter(tags -> !tags.isEmpty())
                .orElseThrow(() -> new ExpectedException(ErrorCode.MISSING_REQUIRED_TAG));
        if (postTags.size() > 5) {
            throw new ExpectedException(ErrorCode.TAG_LIMIT_EXCEEDED);
        }

        Post post = new Post(user, postCreateRequest.getPostCategory(), postCreateRequest.getTitle(),
                postCreateRequest.getContent(), 0, 0, null, null, postCreateRequest.getPostTags());

        // 태그
        for (PostTag postTag: postTags) {
            postTagRepository.save(postTag);
        }

        // 이미지
        processImages(post, postCreateRequest.getPostImages(), postImageService);

        postRepository.save(post);
        return post;
    }

    @Override
    public PostCategory getPostCategory() {
        return PostCategory.QnA;
    }
}