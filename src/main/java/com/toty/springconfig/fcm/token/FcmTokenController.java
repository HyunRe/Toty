package com.toty.springconfig.fcm.token;

import com.toty.base.response.SuccessResponse;
import com.toty.springconfig.fcm.FcmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FcmTokenController {
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
}