package com.toty.post.application;

import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.notification.application.service.NotificationSendService;
import com.toty.notification.dto.request.NotificationSendRequest;
import com.toty.post.domain.model.Post;
import com.toty.post.domain.model.PostLike;
import com.toty.post.domain.repository.PostLikeRepository;
import com.toty.post.domain.repository.PostRepository;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final NotificationSendService notificationSendService;


    // 좋아요 토글 (증감소)
    @Transactional
    public int toggleLikeAction(Long postId, Long userId, String likeAction) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
        Post post = postRepository.findById(postId).orElseThrow(() -> new ExpectedException(ErrorCode.POST_NOT_FOUND));

        if ("Like".equals(likeAction)) { // 좋아요 추가
            if (!postLikeRepository.findByUserAndPost(user, post).isPresent()) {
                postLikeRepository.save(new PostLike(user, post));
                NotificationSendRequest notificationSendRequest = new NotificationSendRequest(
                        post.getUser().getId(),     // 알림 받을 사람
                        userId,                     // 알림 보낸 사람
                        user.getNickname(),         // 알림 보낸 사람 닉네임
                        "Like",                     // 알림 유형
                        postId.toString()           // 관련된 게시글 ID
                );
                notificationSendService.sendNotification(notificationSendRequest);
            }
        }
        if ("unlike".equals(likeAction)) { // 좋아요 취소
            postLikeRepository.findByUserAndPost(user, post).ifPresent(postLikeRepository::delete);
        }

        // 좋아요 개수 갱신
        int likeCount = postLikeRepository.countPostLikesByPost(post);
        post.updateLikeCount(likeCount);

        return likeCount;
    }
}
