package com.toty.springconfig.fcm;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.toty.notification.application.sender.NotificationSender;
import com.toty.notification.domain.model.Notification;
import com.toty.springconfig.fcm.token.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmNotificationSender implements NotificationSender {
    private final FcmTokenRepository fcmTokenRepository;
    private final FcmService fcmService;

    @Override
    public void send(Notification notification) throws FirebaseMessagingException {
        FcmNotificationSendRequest fcmNotificationSendRequest = new FcmNotificationSendRequest(
                notification.getSenderNickname(),
                notification.getMessage(),
                notification.getUrl()
        );

        // 단일 사용자에게 알림 전송
        String userToken = getUserFcmToken(notification.getReceiverId());
        fcmService.sendPushNotification(userToken, fcmNotificationSendRequest);

        // 여러 사용자에게 알림을 보내야 할 경우
        List<String> multipleUserTokens = fcmTokenRepository.findTokensByUserId(notification.getReceiverId());
        if (!multipleUserTokens.isEmpty()) {
            fcmService.sendNotificationToMultipleUsers(multipleUserTokens, fcmNotificationSendRequest);
        }
    }

    private String getUserFcmToken(Long userId) {
        return fcmTokenRepository.findByUserId(userId).orElseThrow().getToken();
    }
}
