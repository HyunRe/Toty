package com.toty.notification.application.sender;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.toty.common.baseException.NotificationSenderNotFoundException;
import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.common.baseException.UnSupportedNotificationTypeException;
import com.toty.notification.application.service.NotificationCreationService;
import com.toty.common.redis.infrastructure.RedisPublisher;
import com.toty.notification.domain.model.Notification;
import com.toty.notification.domain.type.EventType;
import com.toty.notification.dto.request.NotificationSendRequest;
import com.toty.notification.infrastructure.email.application.sender.EmailNotificationSender;
import com.toty.notification.infrastructure.firebase.application.sender.FcmNotificationSender;
import com.toty.notification.infrastructure.sms.application.sender.SmsNotificationSender;
import com.toty.common.sse.application.sender.SseNotificationSender;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class NotificationSenderService {
    private final Map<EventType, List<NotificationSender>> senderMap = new HashMap<>();
    private final UserRepository userRepository;
    private final RedisPublisher redisPublisher;
    private final NotificationCreationService notificationCreationService;

    public NotificationSenderService(List<NotificationSender> senders,
                                     UserRepository userRepository,
                                     RedisPublisher redisPublisher,
                                     NotificationCreationService notificationCreationService) {
        this.userRepository = userRepository;
        this.redisPublisher = redisPublisher;
        this.notificationCreationService = notificationCreationService;

        // 채널별 이벤트 등록
        // SSE: 헤더 알림 패널에 표시 (실시간 + API 조회용)
        registerSender(senders, SseNotificationSender.class,
            EventType.COMMENT,
            EventType.LIKE,
            EventType.FOLLOW
        );

        // FCM: 웹푸시 알림 (브라우저 시스템 알림)
        registerSender(senders, FcmNotificationSender.class,
            EventType.QNA_POST,
            EventType.MENTOR_POST,
            EventType.MENTOR_CHAT
        );

        // Email: 멘토 선정/해제
        registerSender(senders, EmailNotificationSender.class,
            EventType.BECOME_MENTOR,
            EventType.REVOKE_MENTOR
        );

        // SMS: 멘토 선정/해제
        registerSender(senders, SmsNotificationSender.class,
            EventType.BECOME_MENTOR,
            EventType.REVOKE_MENTOR
        );
    }

    // 이벤트 타입당 여러 sender 등록
    private void registerSender(List<NotificationSender> senders, Class<? extends NotificationSender> clazz, EventType... eventTypes) {
        NotificationSender sender = senders.stream()
                .filter(s -> s.getClass().equals(clazz))
                .findFirst()
                .orElseThrow(() -> new NotificationSenderNotFoundException(clazz.getSimpleName()));

        for (EventType eventType : eventTypes) {
            senderMap.computeIfAbsent(eventType, k -> new ArrayList<>()).add(sender);
            log.debug("Registered sender {} for eventType {}", sender.getClass().getSimpleName(), eventType);
        }
    }

    @Transactional
    public void send(NotificationSendRequest notificationSendRequest) throws FirebaseMessagingException, MessagingException {
        Long userId = notificationSendRequest.getReceiverId();
        log.info("[SENDER] ========== 알림 전송 시작 ==========");
        log.info("[SENDER] receiverId={}, eventType={}, fromRedis={}",
                userId, notificationSendRequest.getEventType(), notificationSendRequest.isFromRedis());

        User user = userRepository.findById(userId).orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));

        // 사용자가 알림을 구독한 경우에만 알림을 전송
        if (!user.getUserSubscribeInfo().isNotificationAllowed()) {
            log.info("[SENDER] 사용자 {}는 알림을 구독하지 않음", userId);
            throw new ExpectedException(ErrorCode.NOTIFICATIONS_DISABLED);
        }

        // 알림 생성해서 Redis DB에 저장
        EventType eventType = notificationSendRequest.getEventType();
        List<NotificationSender> senders = senderMap.get(eventType);

        if (senders == null || senders.isEmpty()) {
            throw new UnSupportedNotificationTypeException(eventType);
        }

        log.info("[SENDER] 알림 생성 시작");
        Notification notification = notificationCreationService.createNotification(notificationSendRequest);
        log.info("[SENDER] 알림 생성 완료: id={}, url={}", notification.getId(), notification.getUrl());

        // 이벤트 타입에 등록된 모든 sender 순회하며 발송 (SMS + Email 동시 가능)
        log.info("[SENDER] {} 타입의 sender 개수: {}", eventType, senders.size());
        for (NotificationSender sender : senders) {
            try {
                log.info("[SENDER] {} sender로 전송 시작", sender.getClass().getSimpleName());
                sender.send(notification);
                log.info("[SENDER] {} sender로 전송 완료", sender.getClass().getSimpleName());
            } catch (Exception e) {
                log.error("[SENDER] 알림 전송 실패: receiverId={}, eventType={}, sender={}, error={}",
                        userId, eventType, sender.getClass().getSimpleName(), e.getMessage(), e);
            }
        }

        // Redis 발행 비활성화 (단일 서버 환경에서는 불필요)
        // 다중 서버 환경에서만 활성화 필요
        log.info("[SENDER] Redis 발행 생략 (단일 서버 환경)");

        log.info("[SENDER] ========== 알림 전송 종료 ==========");
    }
}

