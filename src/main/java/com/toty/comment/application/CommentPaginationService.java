package com.toty.comment.application;

import com.toty.comment.domain.model.Comment;
import com.toty.comment.domain.pagination.CommentPaginationStrategy;
import com.toty.comment.domain.repository.CommentRepository;
import com.toty.comment.domain.specification.CommentSpecifications;
import com.toty.comment.dto.response.CommentListResponse;
import com.toty.common.pagination.PaginationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentPaginationService {
    private static final int PAGE_SIZE = 10;  // 기본 페이지 수

    private final CommentRepository commentRepository;
    private final CommentPaginationStrategy commentPaginationStrategy;

    // 게시글 내의 댓글 목록 조회
    @Transactional(readOnly = true)
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
}
