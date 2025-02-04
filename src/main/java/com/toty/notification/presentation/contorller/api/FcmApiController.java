package com.toty.notification.presentation.contorller.api;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.toty.base.response.SuccessResponse;
import com.toty.notification.application.service.FcmService;
import com.toty.notification.domain.model.FcmToken;
import com.toty.notification.presentation.dto.request.FcmNotificationSendRequest;
import com.toty.notification.presentation.dto.request.FcmTokenRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FcmApiController {
    private final FcmService fcmService;

    @PostMapping("/register")
    public ResponseEntity<?> registerFcmToken(@Valid @RequestBody FcmTokenRequest fcmTokenRequest) {
        FcmToken fcmToken = fcmService.saveToken(fcmTokenRequest.getUserId(), fcmTokenRequest.getToken());

        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "FCM 토큰 등록 완료",
                fcmToken
        );

        return ResponseEntity.ok(successResponse);
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@Valid @RequestParam String token,
                                              @Valid @RequestBody FcmNotificationSendRequest fcmNotificationSendRequest) throws FirebaseMessagingException {
        fcmService.sendPushNotification(token, fcmNotificationSendRequest);

        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "푸시 알림 전송 완료",
                null
        );

        return ResponseEntity.ok(successResponse);
    }

    @PostMapping("/send-multiple")
    public ResponseEntity<?> sendNotificationToMultiple(@Valid @RequestBody FcmNotificationSendRequest fcmNotificationSendRequest) throws FirebaseMessagingException {
        List<String> tokens = fcmService.findAllTokens();
        fcmService.sendNotificationToMultipleUsers(tokens, fcmNotificationSendRequest);

        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "푸시 알림 전송 완료",
                null
        );

        return ResponseEntity.ok(successResponse);
    }
}