package com.toty.post.application.strategy.creation;

import com.toty.post.application.PostImageService;
import com.toty.post.domain.model.Post;
import com.toty.post.domain.model.PostCategory;
import com.toty.post.presentation.dto.request.PostCreateRequest;
import com.toty.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KnowledgePostCreationStrategy implements PostCreationStrategy {
    private final PostImageService postImageService;

    @Override
    public Post createPostRequest(PostCreateRequest postCreateRequest, User user) {
        // 사용자 권한 확인
        if (postCreateRequest.getPostCategory().equals(PostCategory.KNOWLEDGE)) { // && user.getRole().equals(Role.USER)
            throw new IllegalArgumentException("해당 사용자는 멘토로 지정 되지 않았습니다.");
        }

        Post post = new Post(user, postCreateRequest.getPostCategory(), postCreateRequest.getTitle(),
                postCreateRequest.getContent(), 0, 0, null, null);

        // 이미지
        processImages(post, postCreateRequest.getPostImages(), postImageService);

        // 댓글

        return post;
    }
}
