package com.toty.springconfig.fcm;

import com.google.firebase.messaging.*;
import com.toty.base.exception.NotificationSendException;
import com.toty.base.exception.UserNotFoundException;
import com.toty.springconfig.fcm.token.FcmToken;
import com.toty.springconfig.fcm.token.FcmTokenRepository;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmService {
    private final UserRepository userRepository;
//    private final FirebaseMessaging firebaseMessaging;
    private final FcmTokenRepository fcmTokenRepository;

    // 토큰 리스트 가져 오기
    @Transactional(readOnly = true)
    public List<String> findAllTokens() {
        return fcmTokenRepository.findAllTokens();
    }

    // 토큰 생성 및 업데이트
    @Transactional
    public FcmToken saveToken(Long userId, String token) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        FcmToken fcmToken = fcmTokenRepository.findByUserId(userId).orElse(new FcmToken(user, token));
        fcmToken.updateToken(token);

        return  fcmTokenRepository.save(fcmToken);
    }

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
    @Async
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
//            firebaseMessaging.sendMulticast(message);
        } catch (Exception e) {
            throw new NotificationSendException(e);
        }
    }
}
