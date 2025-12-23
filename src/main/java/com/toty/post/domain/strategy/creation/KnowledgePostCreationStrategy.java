package com.toty.post.domain.strategy.creation;

import com.toty.common.baseException.NotificationSendException;
import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.following.domain.model.Following;
import com.toty.following.domain.repository.FollowingRepository;
import com.toty.notification.application.service.NotificationSendService;
import com.toty.notification.domain.type.EventType;
import com.toty.notification.dto.request.NotificationSendRequest;
import com.toty.post.domain.model.post.Post;
import com.toty.post.domain.model.post.PostCategory;
import com.toty.post.domain.repository.post.PostRepository;
import com.toty.post.dto.request.PostCreateRequest;
import com.toty.user.domain.model.Role;
import com.toty.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KnowledgePostCreationStrategy implements PostCreationStrategy {
    private final NotificationSendService notificationSendService;
    private final FollowingRepository followingRepository;
    private final PostRepository postRepository;

    @Override
    public Post createPostRequest(PostCreateRequest postCreateRequest, User user) {
        // 사용자 권한 확인
        if (user.getRole().equals(Role.USER)) {
            throw new ExpectedException(ErrorCode.USER_NOT_MENTOR);
        }

        Post post = new Post(user, postCreateRequest.getPostCategory(), postCreateRequest.getTitle(),
                postCreateRequest.getContent(), 0, 0, null, null);

        Post savedPost = postRepository.save(post);

        List<Following> followings = followingRepository.findByToUserId(user.getId());
        for (Following following: followings) {
            NotificationSendRequest notificationSendRequest = new NotificationSendRequest(
                    following.getFromUser().getId(),      // 알림 받을 사람 (팔로워)
                    user.getId(),                         // 알림 보낸 사람 (멘토 본인)
                    user.getNickname(),                   // 알림 보낸 사람 닉네임
                    EventType.MENTOR_POST,                // 이벤트 유형
                    String.valueOf(savedPost.getId()),    // 게시글 ID (저장된 객체 사용)
                    false                                 // RedisSubscriber에서 온 게 아님
            );

            try {
                notificationSendService.sendNotification(notificationSendRequest);
            } catch (Exception e) {
                throw new NotificationSendException(e);
            }
        }

        return savedPost;
    }

    @Override
    public PostCategory getPostCategory() {
        return PostCategory.KNOWLEDGE;
    }
}
