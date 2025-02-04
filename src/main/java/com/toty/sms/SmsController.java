package com.toty.sms;

import com.toty.sms.application.SmsService;
import com.toty.sms.presentation.request.PostReplyNotificationRequest;
import com.toty.sms.presentation.request.SmsChatRoomNotificationRequest;
import com.toty.sms.presentation.request.SmsRequest;
import com.toty.sms.presentation.response.SmsAuthCodeResponse;
import com.toty.sms.presentation.response.SmsResponse;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.message.response.MultipleDetailMessageSentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//@RestController
@RequestMapping("/api/sms")
@RestController
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;

    @PostMapping("/authcode")
    public ResponseEntity<SmsAuthCodeResponse> sendAuthCode(@RequestBody SmsRequest smsRequestDto) {
        SmsAuthCodeResponse smsResponseDto = smsService.sendAuthCodeMessage(smsRequestDto);
        return ResponseEntity.ok().body(smsResponseDto);
    }

    @PostMapping("/alert")
    public ResponseEntity<SmsResponse> sendAlertMessage(@RequestBody PostReplyNotificationRequest smsRequestDto) {
        SmsResponse smsResponseDto = smsService.sendAlertMessage(smsRequestDto);
        return ResponseEntity.ok().body(smsResponseDto);
    }

    @PostMapping("/groupchat-alert")
    public ResponseEntity<MultipleDetailMessageSentResponse> sendAlertMessage(@RequestBody SmsChatRoomNotificationRequest request) {
        MultipleDetailMessageSentResponse smsResponseDto = smsService.sendChatRoomGroup(request);
        return ResponseEntity.ok().body(smsResponseDto);
    }
}
