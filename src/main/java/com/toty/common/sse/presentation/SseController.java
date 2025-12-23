package com.toty.common.sse.presentation;

import com.toty.comment.application.sse.CommentSseService;
import com.toty.common.annotation.CurrentUser;
import com.toty.notification.application.sse.NotificationSseService;
import com.toty.post.application.sse.PostLikeSseService;
import com.toty.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "SSE", description = "Server-Sent Events API")
@Slf4j
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SseController {
    private final CommentSseService commentSseService;
    private final PostLikeSseService postLikeSseService;
    private final NotificationSseService notificationSseService;

    /**
     * 댓글 SSE 구독
     * GET /api/sse/posts/{postId}/comments
     */
    @Operation(summary = "댓글 SSE 구독", description = "특정 게시글의 댓글 실시간 업데이트를 구독합니다")
    @GetMapping(value = "/posts/{postId}/comments", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeComment(@PathVariable Long postId) {
        return commentSseService.subscribe(postId);
    }

    /**
     * 좋아요 SSE 구독
     * GET /api/sse/posts/{postId}/likes
     */
    @Operation(summary = "좋아요 SSE 구독", description = "특정 게시글의 좋아요 실시간 업데이트를 구독합니다")
    @GetMapping(value = "/posts/{postId}/likes", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribePostLikes(@PathVariable Long postId) {
        return postLikeSseService.subscribe(postId);
    }

    /**
     * 알림 SSE 구독 (CurrentUser 리졸버 사용)
     * GET /api/sse/notifications
     */
    @Operation(summary = "알림 SSE 구독", description = "로그인한 사용자의 알림 실시간 업데이트를 구독합니다")
    @GetMapping(value = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribeNotifications(@CurrentUser User user) {
        SseEmitter emitter = notificationSseService.subscribe(user.getId());
        return ResponseEntity.ok(emitter);
    }
}
