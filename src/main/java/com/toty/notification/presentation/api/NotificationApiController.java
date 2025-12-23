package com.toty.notification.presentation.api;

import com.toty.common.response.SuccessResponse;
import com.toty.common.annotation.CurrentUser;
import com.toty.notification.application.service.NotificationService;
import com.toty.notification.domain.model.Notification;
import com.toty.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notification", description = "알림 API")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationApiController {
    private final NotificationService notificationService;

    @Operation(summary = "읽지 않은 알림 개수 조회", description = "로그인한 사용자의 읽지 않은 알림 개수를 조회합니다")
    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadNotificationCount(@CurrentUser User user) {
        int unreadCount = notificationService.countUnreadNotifications(user.getId());
        return ResponseEntity.ok(unreadCount);
    }

    @Operation(summary = "전체 알림 읽음 처리", description = "로그인한 사용자의 모든 알림을 읽음 상태로 변경합니다")
    @PatchMapping("/read-all")
    public ResponseEntity<String> markAllAsRead(@CurrentUser User user) {
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok("true");
    }

    @Operation(summary = "특정 알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다")
    @PatchMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(@PathVariable String id,
                                             @CurrentUser User user) {
        notificationService.markAsReadForReceiverAndNotification(user.getId(), id);
        return ResponseEntity.ok("true");
    }

    // 이 밑은 테스트 용도

    @Operation(summary = "읽은 알림 삭제", description = "[테스트용] 로그인한 사용자의 읽은 알림을 모두 삭제합니다")
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteNotification(@CurrentUser User user) {
        notificationService.deleteAllReadNotifications(user.getId());
        return ResponseEntity.ok("true");
    }

    @Operation(summary = "읽지 않은 알림 목록 조회", description = "로그인한 사용자의 읽지 않은 알림 목록을 날짜순으로 조회합니다")
    @GetMapping("/unread")
    public ResponseEntity<SuccessResponse> getUnreadNotifications(@CurrentUser User user) {
        List<Notification> unreadNotifications = notificationService.getUnreadNotificationsSortedByDate(user.getId());
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "읽지 않은 알림 목록",
                unreadNotifications
        );

        return ResponseEntity.ok(successResponse);
    }
}
