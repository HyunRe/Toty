package com.toty.springconfig.fcm;

import com.google.firebase.messaging.*;
import com.toty.common.baseException.NotificationSendException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmService {
    private final FirebaseMessaging firebaseMessaging;

    // 단일 사용자에게 푸시 알림 전송
    public void sendPushNotification(String token, FcmNotificationSendRequest fcmNotificationSendRequest) throws FirebaseMessagingException {
        String title = fcmNotificationSendRequest.getSenderNickname();
        String body = fcmNotificationSendRequest.getMessage();

        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("url", fcmNotificationSendRequest.getUrl())
                .build();

        try {
//            firebaseMessaging.send(message);
        } catch (Exception e) {
            throw new NotificationSendException(e);
        }
    }

    // 다중 사용자에게 동시에 푸시 알림 전송
    @Async("notificationExecutor")
    public void sendNotificationToMultipleUsers(List<String> tokens, FcmNotificationSendRequest fcmNotificationSendRequest) throws FirebaseMessagingException {
        String title = fcmNotificationSendRequest.getSenderNickname();
        String body = fcmNotificationSendRequest.getMessage();

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("url", fcmNotificationSendRequest.getUrl())
                .build();

        try {
            firebaseMessaging.sendEachForMulticast(message);
        } catch (Exception e) {
            throw new NotificationSendException(e);
        }
    }
}
