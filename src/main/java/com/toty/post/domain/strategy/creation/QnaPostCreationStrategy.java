package com.toty.post.domain.strategy.creation;

import com.toty.common.domain.Tag;
import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.post.domain.model.Post;
import com.toty.post.domain.model.PostCategory;
import com.toty.post.domain.model.PostTag;
import com.toty.post.domain.repository.PostRepository;
import com.toty.post.domain.repository.PostTagRepository;
import com.toty.post.dto.request.PostCreateRequest;
import com.toty.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class QnaPostCreationStrategy implements PostCreationStrategy {
    private final PostTagRepository postTagRepository;
    private final PostRepository postRepository;

    @Override
    public Post createPostRequest(PostCreateRequest postCreateRequest, User user) {
        // 태그 검증
        List<String> tagNames = Optional.ofNullable(postCreateRequest.getPostTags())
                .filter(tags -> !tags.isEmpty())
                .orElseThrow(() -> new ExpectedException(ErrorCode.MISSING_REQUIRED_TAG));

        if (tagNames.size() > 5) {
            throw new ExpectedException(ErrorCode.TAG_LIMIT_EXCEEDED);
        }

        Post post = new Post(user, postCreateRequest.getPostCategory(), postCreateRequest.getTitle(),
                postCreateRequest.getContent(), 0, 0, null, null);

        // 태그
        List<PostTag> postTags = tagNames.stream()
                .map(tag -> new PostTag(post, Tag.fromString(tag))) // String -> Enum -> PostTag
                .toList();
        postTagRepository.saveAll(postTags);

        postRepository.save(post);
        return post;
    }

    @Override
    public PostCategory getPostCategory() {
        return PostCategory.QnA;
    }
}