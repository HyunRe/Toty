package com.toty.comment.application.service;

import com.toty.comment.domain.event.CommentEvent;
import com.toty.comment.domain.model.comment.Comment;
import com.toty.comment.domain.repository.CommentRepository;
import com.toty.comment.dto.CommentDto;
import com.toty.comment.dto.request.CommentCreateUpdateRequest;
import com.toty.common.baseException.NotificationSendException;
import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.notification.application.service.NotificationSendService;
import com.toty.notification.domain.type.EventType;
import com.toty.notification.dto.request.NotificationSendRequest;
import com.toty.post.domain.model.post.Post;
import com.toty.post.domain.model.post.PostCategory;
import com.toty.post.domain.repository.post.PostRepository;
import com.toty.comment.application.sse.CommentSseService;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationSendService notificationSendService;
    private final CommentSseService commentSseService;

    // 댓글 가져 오기
    public Comment findByCommentId(Long id) {
        return commentRepository.findById(id).orElseThrow(() -> new ExpectedException(ErrorCode.COMMENT_NOT_FOUND));
    }

    // 본인 계정 확인
    private void  isOwner(User user, Long postOwnerId) {
        if (!user.getId().equals(postOwnerId)) {
            throw new ExpectedException(ErrorCode.INSUFFICIENT_PERMISSION);
        }
    }

    // 댓글 작성
    @Transactional
    public CommentDto createComment(Long userId, Long postId, CommentCreateUpdateRequest commentCreateUpdateRequest) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
        Post post = postRepository.findById(postId).orElseThrow(() -> new ExpectedException(ErrorCode.POST_NOT_FOUND));

        Comment comment = new Comment(user, post, commentCreateUpdateRequest.getContent());
        post.addComment(comment);
        commentRepository.save(comment);

        // 자기 자신의 게시글에 댓글 단 경우는 알림 X
        if (!userId.equals(post.getUser().getId())) {
            EventType eventType = (post.getPostCategory() == PostCategory.QnA) ? EventType.QNA_POST : EventType.COMMENT;
            NotificationSendRequest notificationSendRequest = new NotificationSendRequest(
                    post.getUser().getId(),     // 알림 받을 사람
                    userId,                     // 알림 보낸 사람
                    user.getNickname(),         // 알림 보낸 사람 닉네임
                    eventType,                  // 이벤트 유형
                    postId.toString(),          // 관련된 게시글 ID
                    false                       // RedisSubscriber에서 온 게 아님
            );

            try {
                notificationSendService.sendNotification(notificationSendRequest);
            } catch (Exception e) {
                throw new NotificationSendException(e);
            }
        }

        // SSE 이벤트 전송 (Comment 엔티티를 DTO로 변환하여 전송)
        CommentDto commentDto = toCommentDto(comment);
        commentSseService.sendComment(postId, new CommentEvent("CREATE", commentDto));

        return commentDto;
    }

    // 댓글 수정
    @Transactional
    public CommentDto updateComment(User user, Long id, CommentCreateUpdateRequest commentCreateUpdateRequest) {
        Comment comment = findByCommentId(id);
        // 내 댓글 인지 확인 필요
        isOwner(user, comment.getUser().getId());

        comment.updateComment(commentCreateUpdateRequest.getContent());
        commentRepository.save(comment);

        // SSE 이벤트 전송
        CommentDto commentDto = toCommentDto(comment);
        commentSseService.sendComment(comment.getPost().getId(), new CommentEvent("UPDATE", commentDto));

        return toCommentDto(comment);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(User user, Long id) {
        Comment comment = findByCommentId(id);
        // 내 댓글 인지 확인 필요
        isOwner(user, comment.getUser().getId());

        // 삭제하기 전에 SSE 이벤트를 먼저 보냅니다. (삭제 후 엔티티 정보 접근 불가할 수 있음)
        CommentDto commentDto = toCommentDto(comment); // 삭제될 댓글 정보
        commentSseService.sendComment(comment.getPost().getId(), new CommentEvent("DELETE", commentDto));


        // 정말로 삭제 할 것인지 확인 필요 - 프론트에서 처리
        commentRepository.delete(comment);
    }

    // Comment 엔티티를 CommentDto로 변환하는 헬퍼 메서드
    private CommentDto toCommentDto(Comment comment) {
        return CommentDto.from(comment);
    }
}
