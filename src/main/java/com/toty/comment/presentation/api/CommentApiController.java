package com.toty.comment.presentation.api;

import com.toty.comment.application.service.CommentPaginationService;
import com.toty.comment.dto.CommentDto;
import com.toty.common.annotation.CurrentUser;
import com.toty.common.pagination.PaginationResult;
import com.toty.comment.application.service.CommentService;
import com.toty.comment.dto.request.CommentCreateUpdateRequest;
import com.toty.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Comment", description = "댓글 API")
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentApiController {
    private final CommentService commentService;
    private final CommentPaginationService commentPaginationService;

    @Operation(summary = "댓글 삭제", description = "특정 댓글을 삭제합니다")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteComment(@CurrentUser User user,
                                                @PathVariable Long id) {
        commentService.deleteComment(user, id);
        return ResponseEntity.ok("true");
    }

    @Operation(summary = "댓글 작성", description = "특정 게시글에 새로운 댓글을 작성합니다")
    @PostMapping("/create")
    public ResponseEntity<CommentDto> createComment(@CurrentUser User user,
                                                    @RequestParam("postId") Long postId,
                                                    @Valid @RequestBody CommentCreateUpdateRequest commentCreateUpdateRequest) {
        CommentDto commentDto = commentService.createComment(user.getId(), postId, commentCreateUpdateRequest);
        return ResponseEntity.ok(commentDto);
    }

    @Operation(summary = "댓글 수정", description = "특정 댓글을 수정합니다")
    @PatchMapping("/{id}")
    public ResponseEntity<CommentDto> updateComment(@CurrentUser User user,
                                                    @PathVariable Long id,
                                                    @Valid @RequestBody CommentCreateUpdateRequest commentCreateUpdateRequest) {
        CommentDto commentDto = commentService.updateComment(user, id, commentCreateUpdateRequest);
        return ResponseEntity.ok(commentDto);
    }

    @Operation(summary = "댓글 목록 조회", description = "특정 게시글의 댓글 목록을 페이지네이션으로 조회합니다")
    @GetMapping("/list")
    public ResponseEntity<PaginationResult> commentList(@RequestParam(name = "page", defaultValue = "1") int page,
                                                        @RequestParam("postId") Long postId) {
        PaginationResult result = commentPaginationService.getPagedCommentsByPostId(page, postId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "내가 작성한 댓글 목록 조회", description = "로그인한 사용자가 작성한 댓글 목록을 페이지네이션으로 조회합니다")
    @GetMapping("/myList")
    public ResponseEntity<PaginationResult> myCommentList(@CurrentUser User user,
                                                          @RequestParam(name = "page", defaultValue = "1") int page) {
        PaginationResult result = commentPaginationService.getPagedCommentsByUserId(page, user.getId());
        return ResponseEntity.ok(result);
    }
}