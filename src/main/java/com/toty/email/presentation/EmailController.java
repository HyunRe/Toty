package com.toty.email.presentation;

import com.toty.email.application.EmailService;
import com.toty.email.domain.EmailMessage;
import com.toty.email.presentation.request.EmailPostDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/send-email")
//@RestController
@RequiredArgsConstructor
public class EmailController { // todo 형식

    private final EmailService emailService;

    @PostMapping("/find-password")
    public ResponseEntity sendPasswordMail(@RequestParam EmailPostDto emailPostDto) {
        if (emailPostDto == null || emailPostDto.getEmailAddress() == null || emailPostDto.getEmailAddress().isEmpty()) {
            return ResponseEntity.badRequest().body("이메일 주소를 입력해주세요.");
        }

        EmailMessage emailMessage = EmailMessage.builder()
                .to(emailPostDto.getEmailAddress())
                .subject("[MyFeed] 비밀번호 찾기")
                .build();

        emailService.sendMail(emailMessage, "password");
        // 성공 시 HTTP 200 OK 상태코드 반환
        return ResponseEntity.ok().build();
    }




}
