package com.toty.notification.application.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.toty.base.exception.NotificationNotFoundException;
import com.toty.notification.application.factory.message.NotificationMessageFactory;
import com.toty.notification.application.factory.url.NotificationUrlFactory;
import com.toty.notification.application.sender.NotificationSenderService;
import com.toty.notification.domain.model.Notification;
import com.toty.notification.domain.repository.NotificationRepository;
import com.toty.notification.presentation.dto.request.NotificationSendRequest;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationSenderService notificationSenderService;
    private final NotificationMessageFactory notificationMessageFactory;
    private final NotificationUrlFactory notificationUrlFactory;

    public void createNotification(NotificationSendRequest notificationSendRequest) throws FirebaseMessagingException, MessagingException {
        String message = notificationMessageFactory.generateMessage(
                notificationSendRequest.getType(),
                notificationSendRequest.getSenderNickname()
        );

        String url = notificationUrlFactory.generateUrl(
                notificationSendRequest.getType(),
                notificationSendRequest.getReferenceId()
        );

        Notification notification = new Notification(
                UUID.randomUUID().toString(),
                notificationSendRequest.getReceiverId(),
                notificationSendRequest.getSenderId(),
                notificationSendRequest.getSenderNickname(),
                notificationSendRequest.getType(),
                message,
                url,
                false,
                LocalDateTime.now()
        );

        // Redis에 저장
        notificationRepository.save(notification);

        // 실시간 전송 (SSE 또는 Pub/Sub)
        notificationSenderService.send(notification);
    }

    // 읽지 않은 알림 조회
    public List<Notification> getUnreadNotifications(Long receiverId) {
        return notificationRepository.findByReceiverIdAndIsReadFalse(receiverId, Sort.by(Sort.Order.desc("createdAt")));
    }

    // 전체 알림 읽음 처리
    public void markAllAsRead(Long receiverId) {
        List<Notification> unreadNotifications = notificationRepository.findByReceiverIdAndIsReadFalse(receiverId, Sort.by(Sort.Order.desc("createdAt")));
        unreadNotifications.forEach(notification -> notification.updateIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    // 읽지 않은 알림 개수 조회
    public int getUnreadNotificationCount(Long receiverId) {
        return notificationRepository.countByReceiverIdAndIsReadFalse(receiverId);
    }

    // 알림 읽음 처리
    public void markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(NotificationNotFoundException::new);
        notification.updateIsRead(true);
        notificationRepository.save(notification);
    }

    // 읽은 알림 삭제
    public void deleteReadNotifications(Long receiverId) {
        notificationRepository.deleteByReceiverIdAndIsReadTrue(receiverId);
    }
}

