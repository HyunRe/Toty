package com.toty.notification.application.service;

import com.toty.notification.domain.factory.message.NotificationMessageFactory;
import com.toty.notification.domain.factory.url.NotificationUrlFactory;
import com.toty.notification.domain.model.Notification;
import com.toty.notification.domain.repository.NotificationRepository;
import com.toty.notification.domain.type.EventType;
import com.toty.notification.dto.request.NotificationSendRequest;
import com.toty.notification.infrastructure.email.application.service.EmailService;
import com.toty.notification.infrastructure.email.dto.EmailNotificationSendRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationCreationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMessageFactory notificationMessageFactory;
    private final NotificationUrlFactory notificationUrlFactory;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public Notification createNotification(NotificationSendRequest notificationSendRequest) {
        log.info("[CREATION] 알림 객체 생성 시작: receiverId={}, eventType={}, referenceId={}",
                notificationSendRequest.getReceiverId(),
                notificationSendRequest.getEventType(),
                notificationSendRequest.getReferenceId());

        String message = notificationMessageFactory.generateMessage(
                notificationSendRequest.getEventType(),
                notificationSendRequest.getSenderNickname()
        );

        String url = notificationUrlFactory.generateUrl(
                notificationSendRequest.getEventType(),
                notificationSendRequest.getReferenceId()
        );

        String notificationId = UUID.randomUUID().toString();
        log.info("[CREATION] 생성된 알림 ID: {}, URL: {}, Message: {}", notificationId, url, message);

        Notification notification = new Notification(
                notificationId,
                notificationSendRequest.getReceiverId(),
                notificationSendRequest.getSenderId(),
                notificationSendRequest.getSenderNickname(),
                notificationSendRequest.getEventType(),
                message,
                url,
                false,
                LocalDateTime.now()
        );

        // Redis DB에 저장
        log.info("[CREATION] Redis에 알림 저장 시작");
        notificationRepository.save(notification);
        log.info("[CREATION] Redis에 알림 저장 완료");

        // 안읽은 알림이 정확히 10개가 되면 이메일 전송
        checkAndSendUnreadNotificationEmail(notificationSendRequest.getReceiverId());

        return notification;
    }

    /**
     * 안읽은 알림이 정확히 10개가 되는 순간 이메일 전송
     */
    private void checkAndSendUnreadNotificationEmail(Long receiverId) {
        try {
            int unreadCount = notificationService.countUnreadNotifications(receiverId);

            // 정확히 10개일 때만 이메일 전송 (10개 초과 시에는 전송하지 않음)
            if (unreadCount == 10) {
                log.info("안읽은 알림이 10개 도달 - 이메일 전송 시작: receiverId={}", receiverId);

                EmailNotificationSendRequest emailRequest = new EmailNotificationSendRequest(
                        receiverId,
                        "확인하지 않은 알림이 10개 있습니다",
                        EventType.LIKE  // unread 템플릿 선택을 위한 임시 EventType (BECOME_MENTOR/REVOKE_MENTOR가 아닌 것)
                );

                emailService.sendEmailNotification(emailRequest);
                log.info("안읽은 알림 이메일 전송 완료: receiverId={}", receiverId);
            }
        } catch (Exception e) {
            // 이메일 전송 실패해도 알림 생성은 성공시킴 (이메일은 부가 기능)
            log.error("안읽은 알림 이메일 전송 실패: receiverId={}, error={}", receiverId, e.getMessage(), e);
        }
    }
}