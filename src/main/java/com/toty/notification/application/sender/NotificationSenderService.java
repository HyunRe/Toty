package com.toty.notification.application.sender;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.toty.common.baseException.NotificationSenderNotFoundException;
import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.common.baseException.UnSupportedNotificationTypeException;
import com.toty.springconfig.redis.RedisPublisher;
import com.toty.notification.domain.model.Notification;
import com.toty.notification.dto.request.NotificationSendRequest;
import com.toty.springconfig.email.EmailNotificationSender;
import com.toty.springconfig.fcm.FcmNotificationSender;
import com.toty.springconfig.sms.SmsNotificationSender;
import com.toty.springconfig.sse.SseNotificationSender;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NotificationSenderService {
    private final Map<String, NotificationSender> senderMap;
    private final UserRepository userRepository;
    private final RedisPublisher redisPublisher;

    public NotificationSenderService(List<NotificationSender> senders,
                                     Map<String, NotificationSender> senderMap,
                                     UserRepository userRepository,
                                     RedisPublisher redisPublisher) {
        this.senderMap = senderMap;
        this.userRepository = userRepository;
        this.redisPublisher = redisPublisher;

        registerSender(senders, SseNotificationSender.class, "Follow", "Comment", "Like");
        registerSender(senders, FcmNotificationSender.class, "ChatRoom", "GroupChatRoom", "Knowledge", "GroupKnowledge", "Q/A");
        registerSender(senders, EmailNotificationSender.class, "Mento");
        registerSender(senders, SmsNotificationSender.class, "Mento");
    }

    // 전략 패턴에 따라 다른 알림 전송
    private void registerSender(List<NotificationSender> senders, Class<? extends NotificationSender> clazz, String... types) {
        NotificationSender sender = senders.stream()
                .filter(s -> s.getClass().equals(clazz))
                .findFirst()
                .orElseThrow(() -> new NotificationSenderNotFoundException(clazz.getSimpleName()));

        for (String type : types) {
            senderMap.put(type, sender);
        }
    }

    public void send(Notification notification) throws FirebaseMessagingException, MessagingException {
        String type = notification.getType();
        Long userId = notification.getReceiverId();
        User user = userRepository.findById(userId).orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));

        // 사용자가 알림을 구독한 경우에만 알림을 전송
        if (!user.getSubscribeInfo().isNotificationAllowed()) {
            throw new ExpectedException(ErrorCode.NOTIFICATIONS_DISABLED);
        }

        NotificationSender sender = senderMap.get(type);
        if (sender != null) {
            sender.send(notification);
        } else {
            throw new UnSupportedNotificationTypeException(type);
        }

        // Redis Pub/Sub을 통해 다른 서버로 알림 전송
        NotificationSendRequest sendRequest = convertToSendRequest(notification);
        redisPublisher.publish(sendRequest);
    }

    public NotificationSendRequest convertToSendRequest(Notification notification) {
        return new NotificationSendRequest(
                notification.getReceiverId(),
                notification.getSenderId(),
                notification.getSenderNickname(),
                notification.getType(),
                notification.getUrl()
        );
    }
}
