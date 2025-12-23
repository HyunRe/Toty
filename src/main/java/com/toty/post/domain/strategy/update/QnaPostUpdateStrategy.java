package com.toty.post.domain.strategy.update;

import com.toty.common.domain.Tag;
import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.post.domain.model.post.Post;
import com.toty.post.domain.model.post.PostCategory;
import com.toty.post.domain.model.post.PostTag;
import com.toty.post.domain.repository.post.PostTagRepository;
import com.toty.post.dto.request.PostUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QnaPostUpdateStrategy implements PostUpdateStrategy {
    private final PostTagRepository postTagRepository;

    @Override
    public Post updatePostRequest(PostUpdateRequest postUpdateRequest, Post post) {
        // 태그 검증
        List<String> tagNames = Optional.ofNullable(postUpdateRequest.getPostTags())
                .filter(tags -> !tags.isEmpty())
                .orElseThrow(() -> new ExpectedException(ErrorCode.MISSING_REQUIRED_TAG));

        if (tagNames.size() > 5) {
            throw new ExpectedException(ErrorCode.TAG_LIMIT_EXCEEDED);
        }

        Post updatedPost = post.updatePost(postUpdateRequest.getTitle(), postUpdateRequest.getContent(), new ArrayList<>());

        // 태그
        updateTags(post, tagNames);

        return updatedPost;
    }

    @Override
    public PostCategory getPostCategory() {
        return PostCategory.QnA;
    }

    @Transactional
    public void updateTags(Post post, List<String> tagNames) {
        // 1. 기존 태그 삭제
        List<PostTag> existingTags = postTagRepository.findByPost(post);
        List<String> tagNamesToDelete = tagNames.stream()
                .map(tag -> Tag.fromString(tag).name())
                .toList();

        // 삭제할 태그들 찾기
        List<PostTag> tagsToDelete = existingTags.stream()
                .filter(postTag -> !tagNamesToDelete.contains(postTag.getTagName().name()))
                .collect(Collectors.toList());

        // 2. 삭제할 태그를 DB에서 삭제
        postTagRepository.deleteAll(tagsToDelete);

        // 3. 새로운 태그 목록 생성
        List<PostTag> newTags = tagNames.stream()
                .map(tag -> new PostTag(post, Tag.fromString(tag)))  // String -> Enum -> PostTag
                .toList();

        // 4. 새로운 태그 추가
        post.getPostTags().clear();
        post.getPostTags().addAll(newTags);

        // 5. 새로운 태그 DB에 저장
        postTagRepository.saveAll(newTags);
    }

}