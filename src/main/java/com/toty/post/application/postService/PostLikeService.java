package com.toty.post.application.postService;

import com.toty.common.baseException.NotificationSendException;
import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.notification.application.service.NotificationSendService;
import com.toty.notification.domain.type.EventType;
import com.toty.notification.dto.request.NotificationSendRequest;
import com.toty.post.domain.event.PostLikeEvent;
import com.toty.post.domain.model.post.Post;
import com.toty.post.domain.model.post.PostLike;
import com.toty.post.domain.repository.post.PostLikeRepository;
import com.toty.post.domain.repository.post.PostRepository;
import com.toty.post.dto.PostLikeDto;
import com.toty.post.application.sse.PostLikeSseService;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostLikeService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final NotificationSendService notificationSendService; // 기존 알림 서비스
    private final PostLikeSseService postLikeSseService; // SSE 좋아요 서비스 추가

    // 좋아요 토글 (증감소)
    @Transactional
    public int toggleLikeAction(Long postId, Long userId, String likeAction) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
        Post post = postRepository.findById(postId).orElseThrow(() -> new ExpectedException(ErrorCode.POST_NOT_FOUND));

        Optional<PostLike> existingLike = postLikeRepository.findByUserAndPost(user, post);

        if ("Like".equals(likeAction)) { // 좋아요 추가
            if (existingLike.isEmpty()) {
                PostLike newLike = new PostLike(user, post);
                postLikeRepository.save(newLike);
                post.incrementLikeCount();

                // 알림 전송 (자기 게시글 좋아요는 알림 X)
                if (!userId.equals(post.getUser().getId())) {
                    NotificationSendRequest notificationSendRequest = new NotificationSendRequest(
                            post.getUser().getId(),     // 알림 받을 사람 (게시글 작성자)
                            userId,                     // 알림 보낸 사람 (좋아요 누른 사람)
                            user.getNickname(),         // 알림 보낸 사람 닉네임
                            EventType.LIKE,             // 알림 유형
                            postId.toString(),          // 관련된 게시글 ID
                            false                       // RedisSubscriber에서 온 게 아님
                    );
                    try {
                        notificationSendService.sendNotification(notificationSendRequest);
                    } catch (Exception e) {
                        throw new NotificationSendException(e);
                    }
                }

                // SSE 이벤트 전송 (좋아요)
                PostLikeDto postLikeDto = toPostLikeDto(newLike);
                PostLikeEvent postLikeEvent = new PostLikeEvent("LIKE", postLikeDto, (long) post.getLikeCount());
                postLikeSseService.sendPostLike(postId, postLikeEvent);
            }
        } else if ("unlike".equals(likeAction)) { // 좋아요 취소
            existingLike.ifPresent(like -> {
                postLikeRepository.delete(like);
                post.decrementLikeCount();

                // SSE 이벤트 전송 (좋아요 취소)
                PostLikeDto postLikeDto = toPostLikeDto(like);
                PostLikeEvent postLikeEvent = new PostLikeEvent("UNLIKE", postLikeDto, (long) post.getLikeCount());
                postLikeSseService.sendPostLike(postId, postLikeEvent);
            });
        }
        // 좋아요 개수 갱신은 post 엔티티에서 이루어지므로, 여기서 따로 likeCount를 다시 계산할 필요가 없습니다.
        return post.getLikeCount(); // Post 엔티티에서 직접 좋아요 수 가져오기
    }

    public boolean isPostLikedByUser(Long postId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.POST_NOT_FOUND));

        return postLikeRepository.existsByUserAndPost(user, post);
    }

    // PostLike 엔티티를 PostLikeDto로 변환하는 헬퍼 메서드
    private PostLikeDto toPostLikeDto(PostLike postLike) {
        return PostLikeDto.from(postLike);
    }
}
