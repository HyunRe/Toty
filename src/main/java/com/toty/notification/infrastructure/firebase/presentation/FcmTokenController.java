package com.toty.notification.infrastructure.firebase.presentation;

import com.toty.common.annotation.CurrentUser;
import com.toty.common.response.SuccessResponse;
import com.toty.notification.infrastructure.firebase.dto.FcmTokenRequest;
import com.toty.notification.infrastructure.firebase.application.service.FcmTokenService;
import com.toty.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "FCM Token", description = "FCM 토큰 관리 API")
@RestController
@RequestMapping("/api/fcm/token")
@RequiredArgsConstructor
public class FcmTokenController {
    private final FcmTokenService fcmTokenService;

    @Operation(summary = "FCM 토큰 등록/업데이트", description = "사용자의 FCM 토큰을 등록하거나 업데이트합니다. 앱 시작 시 호출")
    @PostMapping
    public ResponseEntity<SuccessResponse> saveOrUpdateToken(@CurrentUser User user,
                                                             @Valid @RequestBody FcmTokenRequest request) {

        fcmTokenService.saveOrUpdateToken(user.getId(), request.getToken());

        return ResponseEntity.ok(new SuccessResponse(
                HttpStatus.OK.value(),
                "FCM 토큰이 등록/업데이트되었습니다",
                null
        ));
    }

    @Operation(summary = "FCM 토큰 비활성화", description = "특정 토큰을 비활성화합니다. 로그아웃 시 호출")
    @DeleteMapping
    public ResponseEntity<SuccessResponse> deactivateToken(@CurrentUser User user,
                                                           @Valid @RequestBody FcmTokenRequest request) {

        fcmTokenService.deactivateToken(user.getId(), request.getToken());

        return ResponseEntity.ok(new SuccessResponse(
                HttpStatus.OK.value(),
                "FCM 토큰이 비활성화되었습니다",
                null
        ));
    }

    @Operation(summary = "활성 토큰 조회", description = "로그인한 사용자의 활성화된 FCM 토큰 목록을 조회합니다")
    @GetMapping("/active")
    public ResponseEntity<SuccessResponse> getActiveTokens(@CurrentUser User user) {
        List<String> activeTokens = fcmTokenService.getActiveTokensByUserId(user.getId());

        return ResponseEntity.ok(new SuccessResponse(
                HttpStatus.OK.value(),
                "활성 토큰 목록 조회 성공",
                activeTokens
        ));
    }

    @Operation(summary = "비활성 토큰 삭제", description = "[관리자용] 비활성화된 모든 FCM 토큰을 물리적으로 삭제합니다")
    @DeleteMapping("/deactivate")
    public ResponseEntity<SuccessResponse> deleteInactiveTokens() {
        int deletedCount = fcmTokenService.deleteInactiveTokens();

        return ResponseEntity.ok(new SuccessResponse(
                HttpStatus.OK.value(),
                "비활성 토큰 삭제 완료",
                deletedCount
        ));
    }
}