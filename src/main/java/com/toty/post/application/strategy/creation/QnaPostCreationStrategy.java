package com.toty.post.application.strategy.creation;

import com.toty.post.application.PostImageService;
import com.toty.post.domain.model.Post;
import com.toty.post.domain.model.PostTag;
import com.toty.post.domain.repository.PostTagRepository;
import com.toty.post.presentation.dto.request.PostCreateRequest;
import com.toty.user.domain.model.User;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QnaPostCreationStrategy implements PostCreationStrategy {
    private final PostImageService postImageService;
    private final PostTagRepository postTagRepository;

    @Override
    public Post createPostRequest(PostCreateRequest postCreateRequest, User user) {
        // 태그 검증
        List<PostTag> postTags = postCreateRequest.getPostTags();
        Optional.ofNullable(postTags)
                .filter(tags -> !tags.isEmpty())
                .orElseThrow(() -> new ValidationException("선택된 태그가 반드시 하나 필요 합니다."));
        if (postTags.size() > 5) {
            throw new ValidationException("태그 선택은 최대 5개만 가능 합니다.");
        }

        Post post = new Post(user, postCreateRequest.getPostCategory(), postCreateRequest.getTitle(),
                postCreateRequest.getContent(), 0, 0, null, null, postCreateRequest.getPostTags());

        // 태그
        for (PostTag postTag: postTags) {
            postTagRepository.save(postTag);
        }

        // 이미지
        processImages(post, postCreateRequest.getPostImages(), postImageService);

        return post;
    }
}