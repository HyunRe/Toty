package com.toty.post.application.strategy.update;

import com.toty.post.application.PostImageService;
import com.toty.post.domain.model.Post;
import com.toty.post.domain.model.PostTag;
import com.toty.post.domain.repository.PostTagRepository;
import com.toty.post.presentation.dto.request.PostUpdateRequest;
import com.toty.user.domain.User;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QnaPostUpdateStrategy implements PostUpdateStrategy {
    private final PostImageService postImageService;
    private final PostTagRepository postTagRepository;

    @Override
    public Post updatePostRequest(PostUpdateRequest postUpdateRequest, Post post) {
        // 태그 검증
        List<PostTag> postTags = postUpdateRequest.getPostTags();
        Optional.ofNullable(postTags)
                .filter(tags -> !tags.isEmpty())
                .orElseThrow(() -> new ValidationException("선택된 태그가 반드시 하나 필요 합니다."));
        if (postTags.size() > 5) {
            throw new ValidationException("태그 선택은 최대 5개만 가능 합니다.");
        }

        Post updatedPost = new Post(post.getUser(), post.getPostCategory(), postUpdateRequest.getTitle(), postUpdateRequest.getContent(),
                post.getViewCount(), post.getLikeCount(), null, null, postUpdateRequest.getPostTags());

        // 태그
        for (PostTag postTag: postTags) {
            postTagRepository.save(postTag);
        }

        // 이미지
        synchronizeImages(updatedPost, postUpdateRequest.getPostImages(), postImageService);

        // 댓글

        return updatedPost;
    }
}
