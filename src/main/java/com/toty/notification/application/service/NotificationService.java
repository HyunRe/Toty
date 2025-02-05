package com.toty.notification.application.service;

import com.toty.base.exception.NotificationNotFoundException;
import com.toty.notification.application.eventHandler.NotificationEvent;
import com.toty.notification.domain.model.Notification;
import com.toty.notification.domain.repository.NotificationRepository;
import com.toty.notification.presentation.dto.request.NotificationSendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void sendNotification(NotificationSendRequest notificationSendRequest) {
        eventPublisher.publishEvent(new NotificationEvent(this, notificationSendRequest));
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

