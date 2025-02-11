package com.toty.notification.presentation.api;

import com.toty.base.response.SuccessResponse;
import com.toty.common.annotation.CurrentUser;
import com.toty.notification.application.service.NotificationService;
import com.toty.notification.domain.model.Notification;
import com.toty.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationApiController {
    private final NotificationService notificationService;

    // 읽지 않은 알림 개수
    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadNotificationCount(@CurrentUser User user) {
        int unreadCount = notificationService.countUnreadNotifications(user.getId());
        return ResponseEntity.ok(unreadCount);
    }

    // 전체 알림 읽음 처리
    @PatchMapping("/read-all")
    public ResponseEntity<String> markAllAsRead(@CurrentUser User user) {
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok("true");
    }

    // 읽은 알림으로 전환
    @PatchMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(@PathVariable String id,
                                             @CurrentUser User user) {
        notificationService.markAsReadForReceiverAndNotification(user.getId(), id);
        return ResponseEntity.ok("true");
    }

    // 이 밑은 테스트 용도

    // 읽은 알림 삭제
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteNotification(@CurrentUser User user) {
        notificationService.deleteAllReadNotifications(user.getId());
        return ResponseEntity.ok("true");
    }

    // 읽지 않은 알림 목록 (test)
    @GetMapping("/unread")
    public ResponseEntity<SuccessResponse> getUnreadNotifications(@CurrentUser User user) {
        List<Notification> unreadNotifications = notificationService.getUnreadNotificationsSortedByDate(user.getId());
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "읽지 않은 알림 목록",
                unreadNotifications.size()
        );

        return ResponseEntity.ok(successResponse);
    }
}
