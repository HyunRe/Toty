package com.toty.springconfig.sse;

import com.toty.common.annotation.CurrentUser;
import com.toty.user.domain.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SesController {
    private final SseService sseService;

    // sse 연결
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(@CurrentUser User user) {
        SseEmitter sseEmitter = sseService.createEmitter(user.getId());
        return ResponseEntity.ok(sseEmitter);
    }

    // 개별 알림을 전송하는 엔드포인트
    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@Valid @RequestBody SseNotificationSendRequest sseNotificationSendRequest) {
        sseService.sendNotification(sseNotificationSendRequest);
        return ResponseEntity.ok("true");
    }

    // 다수의 알림을 전송하는 엔드포인트
    @PostMapping("/send-multiple")
    public ResponseEntity<String> sendMultipleNotifications(@Valid @RequestBody List<SseNotificationSendRequest> sseNotificationSendRequests) {
        sseService.sendMultipleNotifications(sseNotificationSendRequests);
        return ResponseEntity.ok("true");
    }
}
