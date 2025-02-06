package com.toty.comment.presentation.api;

import com.toty.comment.application.CommentPaginationService;
import com.toty.common.annotation.CurrentUser;
import com.toty.common.pagination.PaginationResult;
import com.toty.base.response.SuccessResponse;
import com.toty.comment.application.CommentService;
import com.toty.comment.domain.model.Comment;
import com.toty.comment.dto.request.CommentCreateUpdateRequest;
import com.toty.user.domain.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentApiController {
    private final CommentService commentService;
    private final CommentPaginationService commentPaginationService;

    // 댓글 작성
    @PostMapping("/create")
    public ResponseEntity<?> createComment(@RequestParam("userId") Long userId,
                                           @RequestParam("postId") Long postId,
                                           @Valid @RequestBody CommentCreateUpdateRequest commentCreateUpdateRequest) {
        Comment comment = commentService.createComment(userId, postId, commentCreateUpdateRequest);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "댓글 생성 성공",
                comment
        );

        return ResponseEntity.ok(successResponse);
    }

    // 댓글 수정
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateComment(@CurrentUser User user,
                                           @PathVariable Long id,
                                           @Valid @RequestBody CommentCreateUpdateRequest commentCreateUpdateRequest) {
        Comment comment = commentService.updateComment(user, id, commentCreateUpdateRequest);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "댓글 수정 성공",
                comment
        );

        return ResponseEntity.ok(successResponse);
    }

    // 댓글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(@CurrentUser User user,
                                           @PathVariable Long id) {
        commentService.deleteComment(user, id);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "댓글 삭제 성공",
                null
        );

        return ResponseEntity.ok(successResponse);
    }

    // 게시글 내 댓글 목록 조회
    @GetMapping("/list")
    public ResponseEntity<?> commentList(@RequestParam(name = "page", defaultValue = "1") int page,
                                         @RequestParam("postId") Long postId) {
        PaginationResult result = commentPaginationService.getPagedCommentsByPostId(page, postId);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "게시글 내 댓글 목록 조회",
                null
        );

        return ResponseEntity.ok(successResponse);
    }
}
