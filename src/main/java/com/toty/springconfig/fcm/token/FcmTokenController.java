package com.toty.springconfig.fcm.token;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FcmTokenController {
    private final FCmTokenService fCmTokenService;

    @PostMapping("/register")
    public ResponseEntity<FcmToken> registerFcmToken(@Valid @RequestBody FcmTokenRequest fcmTokenRequest) {
        FcmToken fcmToken = fCmTokenService.saveToken(fcmTokenRequest.getUserId(), fcmTokenRequest.getToken());
        return ResponseEntity.ok(fcmToken);
    }
}