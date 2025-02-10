package com.toty.springconfig.fcm.token;

import com.toty.common.annotation.CurrentUser;
import com.toty.user.domain.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fcmToken")
@RequiredArgsConstructor
public class FcmTokenController {
    private final FCmTokenService fCmTokenService;

    @PostMapping("/register")
    public ResponseEntity<FcmToken> registerFcmToken(@CurrentUser User user,
                                                     @Valid @RequestBody FcmTokenRequest fcmTokenRequest) {
        FcmToken fcmToken = fCmTokenService.saveToken(user.getId(), fcmTokenRequest.getToken());
        return ResponseEntity.ok(fcmToken);
    }
}