package com.toty.springconfig.fcm;

import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FcmController {
    private final FcmService fcmService;

    // 단일 사용자에게 푸시 알림 전송
    @PostMapping("/single")
    public ResponseEntity<String> sendPushNotification(@RequestParam String token,
                                                       @Valid @RequestBody FcmNotificationSendRequest fcmNotificationSendRequest) throws FirebaseMessagingException {
        fcmService.sendPushNotification(token, fcmNotificationSendRequest);
        return ResponseEntity.ok("true");
    }

    // 다중 사용자에게 푸시 알림 전송
    @PostMapping("/multiple")
    public ResponseEntity<String> sendNotificationToMultipleUsers(@Valid @RequestBody FcmNotificationSendRequest fcmNotificationSendRequest,
                                                                  @Valid @RequestParam List<String> tokens) throws FirebaseMessagingException {
        fcmService.sendNotificationToMultipleUsers(tokens, fcmNotificationSendRequest);
        return ResponseEntity.ok("true");
    }
}
