package com.toty.notification.infrastructure.firebase.application.sender;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.toty.notification.application.sender.NotificationSender;
import com.toty.notification.domain.model.Notification;
import com.toty.notification.infrastructure.firebase.application.service.FcmTokenService;
import com.toty.notification.infrastructure.firebase.application.service.FirebaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmNotificationSender implements NotificationSender {
    private final FcmTokenService fcmTokenService;
    private final FirebaseService firebaseService;

    @Override
    public void send(Notification notification) throws FirebaseMessagingException {
        Long receiverId = notification.getReceiverId();

        // 사용자의 활성화된 모든 FCM 토큰 조회
        List<String> activeTokens = fcmTokenService.getActiveTokensByUserId(receiverId);

        if (activeTokens.isEmpty()) {
            log.warn("[FCM] No active tokens for user={}", receiverId);
            return;
        }

        // 알림 제목 생성
        String title = "TOTY - " + getEventTypeTitle(notification.getEventType().name());

        log.info("[FCM] 알림 전송 준비: receiverId={}, title={}, body={}, tokens={}",
                receiverId, title, notification.getMessage(), activeTokens.size());

        // FirebaseService를 통해 알림 전송
        firebaseService.sendFcmNotification(
                activeTokens,                           // 토큰 목록
                title,                                  // title
                notification.getMessage(),              // body
                notification.getEventType().name(),     // type
                notification.getId(),                   // notificationId
                notification.getUrl(),                  // url
                null                                    // extraData (필요시 추가)
        );
    }

    private String getEventTypeTitle(String eventType) {
        return switch (eventType) {
            case "COMMENT" -> "새 댓글";
            case "LIKE" -> "좋아요";
            case "FOLLOW" -> "새 팔로워";
            case "MENTOR_POST" -> "멘토 게시글";
            case "QNA_POST" -> "QnA 답변";
            case "MENTOR_CHAT" -> "멘토 채팅";
            case "BECOME_MENTOR" -> "멘토 선정";
            case "REVOKE_MENTOR" -> "멘토 해제";
            default -> "새 알림";
        };
    }
}
