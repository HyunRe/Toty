package com.toty.notification.infrastructure.firebase.presentation;

import com.toty.common.annotation.CurrentUser;
import com.toty.common.response.SuccessResponse;
import com.toty.notification.infrastructure.firebase.application.service.FcmTokenService;
import com.toty.notification.infrastructure.firebase.application.service.FirebaseService;
import com.toty.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Tag(name = "Firebase", description = "FCM 푸시 알림 전송 API (테스트용)")
@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FirebaseController {
    private final FirebaseService firebaseService;
    private final FcmTokenService fcmTokenService;

    @Operation(summary = "단일 사용자에게 푸시 알림 전송", description = "특정 토큰으로 FCM 푸시 알림을 전송합니다 (테스트용)")
    @PostMapping("/send-single")
    public ResponseEntity<SuccessResponse> sendToSingleToken(@Parameter(description = "FCM 토큰") @RequestParam @NotBlank String token,
                                                             @Parameter(description = "알림 제목") @RequestParam @NotBlank String title,
                                                             @Parameter(description = "알림 본문") @RequestParam @NotBlank String body,
                                                             @Parameter(description = "알림 타입") @RequestParam(defaultValue = "TEST") String type,
                                                             @Parameter(description = "알림 ID") @RequestParam(defaultValue = "test-notification") String notificationId,
                                                             @Parameter(description = "클릭 시 이동할 URL") @RequestParam(required = false) String url) {

        firebaseService.sendFcmNotification(Collections.singletonList(token), title, body, type, notificationId, url, null);

        return ResponseEntity.ok(new SuccessResponse(
                HttpStatus.OK.value(),
                "FCM 알림이 전송되었습니다",
                null
        ));
    }

    @Operation(summary = "여러 토큰에게 푸시 알림 전송", description = "여러 FCM 토큰으로 푸시 알림을 일괄 전송합니다 (테스트용)")
    @PostMapping("/send-multiple")
    public ResponseEntity<SuccessResponse> sendToMultipleTokens(@Parameter(description = "FCM 토큰 목록") @RequestBody List<String> tokens,
                                                                @Parameter(description = "알림 제목") @RequestParam @NotBlank String title,
                                                                @Parameter(description = "알림 본문") @RequestParam @NotBlank String body,
                                                                @Parameter(description = "알림 타입") @RequestParam(defaultValue = "TEST") String type,
                                                                @Parameter(description = "알림 ID") @RequestParam(defaultValue = "test-notification") String notificationId,
                                                                @Parameter(description = "클릭 시 이동할 URL") @RequestParam(required = false) String url) {

        firebaseService.sendFcmNotification(tokens, title, body, type, notificationId, url, null);

        return ResponseEntity.ok(new SuccessResponse(
                HttpStatus.OK.value(),
                "FCM 알림이 " + tokens.size() + "개 토큰으로 전송되었습니다",
                null
        ));
    }

    @Operation(summary = "로그인한 사용자에게 푸시 알림 전송", description = "현재 로그인한 사용자의 모든 활성 기기로 FCM 알림을 전송합니다 (테스트용)")
    @PostMapping("/send-to-me")
    public ResponseEntity<SuccessResponse> sendToCurrentUser(@CurrentUser User user,
                                                             @Parameter(description = "알림 제목") @RequestParam @NotBlank String title,
                                                             @Parameter(description = "알림 본문") @RequestParam @NotBlank String body,
                                                             @Parameter(description = "알림 타입") @RequestParam(defaultValue = "TEST") String type,
                                                             @Parameter(description = "클릭 시 이동할 URL") @RequestParam(required = false) String url) {

        List<String> activeTokens = fcmTokenService.getActiveTokensByUserId(user.getId());

        if (activeTokens.isEmpty()) {
            return ResponseEntity.ok(new SuccessResponse(
                    HttpStatus.OK.value(),
                    "활성화된 FCM 토큰이 없습니다",
                    null
            ));
        }

        firebaseService.sendFcmNotification(activeTokens, title, body, type, "test-" + System.currentTimeMillis(), url, null);

        return ResponseEntity.ok(new SuccessResponse(
                HttpStatus.OK.value(),
                "FCM 알림이 " + activeTokens.size() + "개 기기로 전송되었습니다",
                Map.of("deviceCount", activeTokens.size())
        ));
    }

    @Operation(summary = "여러 사용자에게 푸시 알림 전송", description = "여러 사용자의 모든 활성 기기로 FCM 알림을 일괄 전송합니다 (테스트용)")
    @PostMapping("/send-to-users")
    public ResponseEntity<SuccessResponse> sendToMultipleUsers(@Parameter(description = "사용자 ID 목록") @RequestBody List<Long> userIds,
                                                               @Parameter(description = "알림 제목") @RequestParam @NotBlank String title,
                                                               @Parameter(description = "알림 본문") @RequestParam @NotBlank String body,
                                                               @Parameter(description = "알림 타입") @RequestParam(defaultValue = "ANNOUNCEMENT") String type,
                                                               @Parameter(description = "클릭 시 이동할 URL") @RequestParam(required = false) String url) {

        List<String> activeTokens = fcmTokenService.getActiveTokensByUserIds(userIds);

        if (activeTokens.isEmpty()) {
            return ResponseEntity.ok(new SuccessResponse(
                    HttpStatus.OK.value(),
                    "활성화된 FCM 토큰이 없습니다",
                    null
            ));
        }

        firebaseService.sendFcmNotification(activeTokens, title, body, type, "announcement-" + System.currentTimeMillis(), url, null);

        return ResponseEntity.ok(new SuccessResponse(
                HttpStatus.OK.value(),
                userIds.size() + "명의 사용자(" + activeTokens.size() + "개 기기)에게 FCM 알림이 전송되었습니다",
                Map.of("userCount", userIds.size(), "deviceCount", activeTokens.size())
        ));
    }
}
