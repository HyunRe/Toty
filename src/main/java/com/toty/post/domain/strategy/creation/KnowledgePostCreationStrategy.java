package com.toty.post.domain.strategy.creation;

import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.following.domain.Following;
import com.toty.following.domain.FollowingRepository;
import com.toty.notification.application.service.NotificationSendService;
import com.toty.notification.dto.request.NotificationSendRequest;
import com.toty.post.application.PostImageService;
import com.toty.post.domain.model.Post;
import com.toty.post.domain.model.PostCategory;
import com.toty.post.dto.request.PostCreateRequest;
import com.toty.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KnowledgePostCreationStrategy implements PostCreationStrategy {
    private final PostImageService postImageService;
    private final NotificationSendService notificationSendService;
    private final FollowingRepository followingRepository;

    @Override
    public Post createPostRequest(PostCreateRequest postCreateRequest, User user) {
        // 사용자 권한 확인
        if (postCreateRequest.getPostCategory().equals(PostCategory.KNOWLEDGE)) { // && user.getRole().equals(Role.USER)
            throw new ExpectedException(ErrorCode.USER_NOT_MENTOR);
        }

        Post post = new Post(user, postCreateRequest.getPostCategory(), postCreateRequest.getTitle(),
                postCreateRequest.getContent(), 0, 0, null, null, null);

        // 이미지
        processImages(post, postCreateRequest.getPostImages(), postImageService);

        List<Following> followings = followingRepository.findByToUserId(user.getId());
        for (Following following: followings) {
            NotificationSendRequest notificationSendRequest = new NotificationSendRequest(
                    following.getId(),          // 알림 받을 사람
                    user.getId(),               // 알림 보낸 사람
                    user.getNickname(),         // 알림 보낸 사람 닉네임
                    "Knowledge",                // 알림 유형
                    post.toString()             // 관련된 게시글 ID
            );
            notificationSendService.sendNotification(notificationSendRequest);
        }

        return post;
    }

    @Override
    public PostCategory getPostCategory() {
        return PostCategory.KNOWLEDGE;
    }
}
