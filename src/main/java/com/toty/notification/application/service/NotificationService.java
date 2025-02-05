package com.toty.notification.application.service;

import com.toty.base.exception.NotificationNotFoundException;
import com.toty.notification.domain.model.Notification;
import com.toty.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    // 읽지 않은 알림 조회
    public List<Notification> getUnreadNotificationsSortedByDate(Long receiverId) {
        return notificationRepository.findByReceiverIdAndIsReadFalse(receiverId, Sort.by(Sort.Order.desc("createdAt")));
    }

    // 전체 알림 읽음 처리 (비동기 처리)
    @Async
    public void markAllAsRead(Long receiverId) {
        List<Notification> unreadNotifications = notificationRepository.findByReceiverIdAndIsReadFalse(receiverId, Sort.by(Sort.Order.desc("createdAt")));
        unreadNotifications.forEach(notification -> notification.updateIsRead(true));
        notificationRepository.saveAll(unreadNotifications);

        // 1분 후에 읽은 알림 삭제
        try {
            TimeUnit.MINUTES.sleep(1);
            deleteAllReadNotifications(receiverId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // 읽지 않은 알림 개수 조회
    public int countUnreadNotifications(Long receiverId) {
        return notificationRepository.countByReceiverIdAndIsReadFalse(receiverId);
    }

    // 알림 읽음 처리 (비동기 처리)
    @Async
    public void markAsReadForReceiverAndNotification(Long receiverId, String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .filter(n -> n.getReceiverId().equals(receiverId) && !n.isRead())
                .orElseThrow(NotificationNotFoundException::new);
        notification.updateIsRead(true);
        notificationRepository.save(notification);

        // 1분 후에 읽은 알림 삭제
        try {
            TimeUnit.MINUTES.sleep(1);
            deleteAllReadNotifications(receiverId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // 읽은 알림 삭제
    public void deleteAllReadNotifications(Long receiverId) {
        notificationRepository.deleteByReceiverIdAndIsReadTrue(receiverId);
    }
}

