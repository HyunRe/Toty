package com.toty.comment.application;

import com.toty.comment.domain.model.Comment;
import com.toty.comment.domain.repository.CommentRepository;
import com.toty.comment.dto.request.CommentCreateUpdateRequest;
import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.notification.application.service.NotificationSendService;
import com.toty.notification.dto.request.NotificationSendRequest;
import com.toty.post.domain.model.Post;
import com.toty.post.domain.repository.PostRepository;
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

    // 댓글 가져 오기
    public Comment findByCommentId(Long id) {
        return commentRepository.findById(id).orElseThrow(() -> new ExpectedException(ErrorCode.COMMENT_NOT_FOUND));
    }

    // 댓글 작성
    @Transactional
    public Comment createComment(Long userId, Long postId, CommentCreateUpdateRequest commentCreateUpdateRequest) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
        Post post = postRepository.findById(postId).orElseThrow(() -> new ExpectedException(ErrorCode.POST_NOT_FOUND));

        Comment comment = new Comment(user, post, commentCreateUpdateRequest.getContent());
        post.addComment(comment);

        String type = "";
        if (post.getPostCategory().name().equals("Qna")) {
            type = "Qna";
        } else {
            type = "Comment";
        }

        NotificationSendRequest notificationSendRequest = new NotificationSendRequest(
                post.getUser().getId(),     // 알림 받을 사람
                userId,                     // 알림 보낸 사람
                user.getNickname(),         // 알림 보낸 사람 닉네임
                type,                       // 알림 유형
                postId.toString()           // 관련된 게시글 ID
        );
        notificationSendService.sendNotification(notificationSendRequest);

        commentRepository.save(comment);
        return comment;
    }

    // 본인 계정 확인
    private boolean isOwner(User user, Long postOwnerId) {
        return user.getId().equals(postOwnerId);
    }

    // 댓글 수정
    @Transactional
    public Comment updateComment(User user, Long id, CommentCreateUpdateRequest commentCreateUpdateRequest) {
        Comment comment = findByCommentId(id);
        // 내 댓글 인지 확인 필요
//        if (isOwner(user, comment.getUser().getId())) {
//            throw new ExpectedException(ErrorCode.INSUFFICIENT_PERMISSION);
//        }

        Comment updatedComment = new Comment(comment.getUser(), comment.getPost(), commentCreateUpdateRequest.getContent());
        comment.getPost().addComment(updatedComment);
        commentRepository.save(comment);
        return updatedComment;
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(User user, Long id) {
        Comment comment = findByCommentId(id);
        // 내 댓글 인지 확인 필요
//        if (isOwner(user, comment.getUser().getId())) {
//            throw new ExpectedException(ErrorCode.INSUFFICIENT_PERMISSION);
//        }

        // 정말로 삭제 할 것인지 확인 필요 - 프론트에서 처리
        commentRepository.delete(comment);
    }
}
