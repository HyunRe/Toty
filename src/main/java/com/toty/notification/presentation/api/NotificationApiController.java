package com.toty.notification.presentation.api;

import com.toty.base.response.SuccessResponse;
import com.toty.notification.application.service.NotificationService;
import com.toty.springconfig.sse.SseService;
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
    public ResponseEntity<SseEmitter> subscribe(@RequestParam Long userId) {
        SseEmitter sseEmitter = sseService.createEmitter(userId);
        return ResponseEntity.ok(sseEmitter);
    }

    // 읽지 않은 알림 개수
    @GetMapping("/unread/count")
    public ResponseEntity<Integer> getUnreadNotificationCount(@RequestParam Long receiverId) {
        int unreadCount = notificationService.countUnreadNotifications(receiverId);
        return ResponseEntity.ok(unreadCount);
    }

    // 전체 알림 읽음 처리
    @PutMapping("/read/all")
    public ResponseEntity<String> markAllAsRead(@RequestParam Long receiverId) {
        notificationService.markAllAsRead(receiverId);
        return ResponseEntity.ok("true");
    }

    // 읽은 알림으로 전환
    @PostMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(@PathVariable String id,
                                             @RequestParam Long receiverId) {
        notificationService.markAsReadForReceiverAndNotification(receiverId, id);
        return ResponseEntity.ok("true");
    }

    // 읽은 알림 삭제
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteNotification(@RequestParam Long userId) {
        notificationService.deleteAllReadNotifications(userId);
        return ResponseEntity.ok("true");
    }

    // 이 밑은 테스트 용도

    // 읽지 않은 알림 목록 (test)
    @GetMapping("/unread")
    public ResponseEntity<SuccessResponse> getUnreadNotifications(@RequestParam Long userId) {
        List<Notification> unreadNotifications = notificationService.getUnreadNotificationsSortedByDate(userId);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "읽지 않은 알림 목록",
                unreadNotifications.size()
        );

        return ResponseEntity.ok(successResponse);
    }
}
