package com.toty.notification.application.sender;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.toty.base.exception.*;
import com.toty.notification.domain.model.Notification;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationSenderService {
    private final Map<String, NotificationSender> senderMap;
    private final UserRepository userRepository;

    public NotificationSenderService(List<NotificationSender> senders, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.senderMap = senders.stream().collect(Collectors.toMap(
                sender -> getNotificationType(sender.getClass()), sender -> sender
        ));
    }

    public void send(Notification notification) throws FirebaseMessagingException, MessagingException {
        String type = notification.getType();
        Long userId = notification.getReceiverId();
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        // 사용자가 알림을 구독한 경우에만 알림을 전송
        if (!user.getSubscribeInfo().isNotification()) {
            throw new NotificationDisabledException();
        }

        NotificationSender sender = senderMap.get(type);
        if (sender != null) {
            sender.send(notification);
        } else {
            throw new UnsupportedNotificationTypeException(type);
        }
    }

    // 전략 패턴에 따라 다른 알림 전송
    private String getNotificationType(Class<?> clazz) {
        if (clazz == SseNotificationSender.class) return "LIST";
        if (clazz == FcmNotificationSender.class) return "PUSH";
        if (clazz == EmailNotificationSender.class) return "EMAIL";
        if (clazz == SmsNotificationSender.class) return "MESSAGE";
        return null;
    }
}
