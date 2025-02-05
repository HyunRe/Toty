package com.toty.notification.domain.repository;

import com.toty.notification.domain.model.Notification;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NotificationRepository extends CrudRepository<Notification, String> {
    // 읽지 않은 알림 조회
    List<Notification> findByReceiverIdAndIsReadFalse(Long receiverId, Sort sort);

    // 읽지 않은 알림 개수 조회
    int countByReceiverIdAndIsReadFalse(Long receiverId);

    // 읽은 알림 삭제
    void deleteByReceiverIdAndIsReadTrue(Long receiverId);
}