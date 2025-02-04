package com.toty.notification.presentation.contorller.api;

import com.toty.base.response.SuccessResponse;
import com.toty.notification.application.service.NotificationService;
import com.toty.notification.application.service.SseService;
import com.toty.notification.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationApiController {
    private final NotificationService notificationService;
    private final SseService sseService;

    // sse 연결
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<?> subscribe(@RequestParam Long userId) {
        SseEmitter sseEmitter = sseService.createEmitter(userId);

        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "SSE 연결 완료",
                sseEmitter
        );

        return ResponseEntity.ok(successResponse);
    }

    // 읽지 않은 알림 목록
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(@RequestParam Long userId) {
        List<Notification> unreadNotifications = notificationService.getUnreadNotifications(userId);

        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "읽지 않은 알림 목록",
                null
        );

        return ResponseEntity.ok(successResponse);
    }

    // 읽지 않은 알림 개수
    @GetMapping("/unread/count")
    public ResponseEntity<?> getUnreadNotificationCount(@RequestParam Long receiverId) {
        int unreadCount = notificationService.getUnreadNotificationCount(receiverId);

        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "읽지 않은 알림 개수",
                unreadCount
        );

        return ResponseEntity.ok(successResponse);
    }

    // 전체 알림 읽음 처리
    @PutMapping("/read/all")
    public ResponseEntity<?> markAllAsRead(@RequestParam Long receiverId) {
        notificationService.markAllAsRead(receiverId);

        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "전체 알림 읽음 처리",
                null
        );

        return ResponseEntity.ok(successResponse);
    }

    // 읽은 알림으로 전환
    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable String id) {
        notificationService.markAsRead(id);

        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "읽은 알림으로 전환",
                null
        );

        return ResponseEntity.ok(successResponse);
    }

    // 읽은 알림 삭제
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteNotification(@RequestParam Long userId) {
        notificationService.deleteReadNotifications(userId);

        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "읽은 알림 삭제 설공",
                null
        );

        return ResponseEntity.ok(successResponse);
    }
}
