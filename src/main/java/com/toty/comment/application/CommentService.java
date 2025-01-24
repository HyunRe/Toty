package com.toty.comment.application;

import com.toty.base.exception.PostNotFoundException;
import com.toty.base.exception.UserNotFoundException;
import com.toty.base.pagination.PaginationResult;
import com.toty.comment.domain.model.Comment;
import com.toty.comment.domain.pagination.CommentPaginationStrategy;
import com.toty.comment.domain.repository.CommentRepository;
import com.toty.comment.domain.repository.CommentSpecifications;
import com.toty.comment.presentation.dto.request.CommentCreateUpdateRequest;
import com.toty.comment.presentation.dto.response.CommentListResponse;
import com.toty.post.domain.model.Post;
import com.toty.post.domain.repository.PostRepository;
import com.toty.user.domain.User;
import com.toty.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentPaginationStrategy commentPaginationStrategy;

    private static final int PAGE_SIZE = 10;  // 기본 페이지 수

    // 댓글 가져 오기
    public Comment findByCommentId(Long id) {
        return commentRepository.findById(id).orElseThrow(PostNotFoundException::new);
    }

    // 댓글 작성
    public Comment createComment(Long userId, Long postId, CommentCreateUpdateRequest commentCreateUpdateRequest) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        Comment comment = new Comment(user, post, commentCreateUpdateRequest.getContent());
        post.addComment(comment);
        return comment;
    }

    // 댓글 수정
    public Comment updateComment(Long id, CommentCreateUpdateRequest commentCreateUpdateRequest) {
        Comment comment = findByCommentId(id);
        // 내 댓글 인지 확인 필요
//        if (!isOwner(id)) {
//            throw new UnauthorizedException();
//        }

        Comment updatedComment = new Comment(comment.getUser(), comment.getPost(), commentCreateUpdateRequest.getContent());
        comment.getPost().addComment(updatedComment);
        return updatedComment;
    }

    // 댓글 삭제
    public void deleteComment(Long id) {
        Comment comment = findByCommentId(id);
        // 내 댓글 인지 확인 필요
//        if (!isOwner(id)) {
//            throw new UnauthorizedException();
//        }

        // 정말로 삭제 할 것인지 확인 필요 - 프론트에서 처리
        commentRepository.delete(comment);
    }

    // 게시글 내의 댓글 목록 조회
    public PaginationResult getPagedCommentsByPostId(int page, Long postId) {
        PageRequest pageRequest = PageRequest.of(page - 1, PAGE_SIZE, Sort.by(Sort.Order.asc("updatedAt")));
        Page<Comment> comments = commentRepository.findAll(
                Specification.where(CommentSpecifications.isNotDeleted())
                        .and(CommentSpecifications.hasPostId(postId)),
                pageRequest
        );

        // Comment -> CommentListResponse
        List<CommentListResponse> commentLists = comments.getContent().stream()
                .map(this::toCommentListResponse)
                .toList();

        return commentPaginationStrategy.getPaginationResult(comments, PAGE_SIZE, commentLists);
    }

    // Comment -> CommentListResponse
    private CommentListResponse toCommentListResponse(Comment comment) {
        return new CommentListResponse(
                comment.getUser().getNickname(),                   // 사용자 닉네임
                comment.getUser().getProfileImageUrl(),            // 프로필 이미지 URL
                comment.getContent(),                              // 제목
                comment.getUpdatedAt()                             // 생성 일시과 수정 일시 중 더 나중에 된 시간
        );
    }

//    public boolean isOwner(Long id) {
//        Long userId = getCurrentUserId(); // 인증된 사용자 ID 가져오기
//        Comment comment = findByCommentById(id);
//        return post.getAuthor().getId().equals(userId); // 게시글 작성자와 비교
//    }
//
//    private Long getCurrentUserId() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || !(authentication.getPrincipal() instanceof Long)) {
//            throw new UnauthorizedException("인증 정보가 없습니다.");
//        }
//        return (Long) authentication.getPrincipal();
//    }
}
