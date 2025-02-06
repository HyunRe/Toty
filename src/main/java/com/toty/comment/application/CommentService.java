package com.toty.comment.application;

import com.toty.base.exception.PostNotFoundException;
import com.toty.base.exception.UnauthorizedException;
import com.toty.base.exception.UserNotFoundException;
import com.toty.comment.domain.model.Comment;
import com.toty.comment.domain.repository.CommentRepository;
import com.toty.comment.dto.request.CommentCreateUpdateRequest;
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

    // 댓글 가져 오기
    public Comment findByCommentId(Long id) {
        return commentRepository.findById(id).orElseThrow(PostNotFoundException::new);
    }

    // 댓글 작성
    @Transactional
    public Comment createComment(Long userId, Long postId, CommentCreateUpdateRequest commentCreateUpdateRequest) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        Comment comment = new Comment(user, post, commentCreateUpdateRequest.getContent());
        post.addComment(comment);
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
        if (isOwner(user, comment.getUser().getId())) {
            throw new UnauthorizedException();
        }

        Comment updatedComment = new Comment(comment.getUser(), comment.getPost(), commentCreateUpdateRequest.getContent());
        comment.getPost().addComment(updatedComment);
        return updatedComment;
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(User user, Long id) {
        Comment comment = findByCommentId(id);
        // 내 댓글 인지 확인 필요
        if (isOwner(user, comment.getUser().getId())) {
            throw new UnauthorizedException();
        }

        // 정말로 삭제 할 것인지 확인 필요 - 프론트에서 처리
        commentRepository.delete(comment);
    }
}
