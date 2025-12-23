package com.toty.user.application;

import com.toty.common.redis.application.RedisService;
import com.toty.user.domain.model.LoginProvider;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import com.toty.user.dto.request.UserSignUpRequest;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserSignUpService {

    private final UserRepository userRepository;
    private final RedisService redisService;
    private DefaultMessageService messageService;

    @Value("${spring.coolsms.api.key}")
    private String apiKey;

    @Value("${spring.coolsms.api.secret}")
    private String apiSecret;

    @Value("${spring.coolsms.api.fromnumber}")
    private String messageFrom;

    @PostConstruct
    public void initMessageService() {
        System.out.println("========== CoolSMS 초기화 ==========");
        System.out.println("API Key: " + (apiKey != null ? apiKey.substring(0, Math.min(5, apiKey.length())) + "..." : "null"));
        System.out.println("From Number: " + messageFrom);
        this.messageService = NurigoApp.INSTANCE.initialize(
                apiKey,
                apiSecret,
                "https://api.coolsms.co.kr"
        );
        System.out.println("CoolSMS 초기화 완료!");
    }

    @Transactional
    public Long signUp(UserSignUpRequest userSignUpRequest) {
        if(userRepository.existsByEmail(userSignUpRequest.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 사용자입니다.");
        }

        String hashedPwd = BCrypt.hashpw(userSignUpRequest.getPassword(), BCrypt.gensalt());

        User user = User.builder()
                .email(userSignUpRequest.getEmail())
                .password(hashedPwd)
                .username(userSignUpRequest.getUsername())
                .nickname(userSignUpRequest.getNickname())
                .phoneNumber(userSignUpRequest.getPhoneNumber())
                .loginProvider(LoginProvider.FORM)
                .smsSubscribed(false)
                .emailSubscribed(false)
                .notificationAllowed(false)
                .build();

        return userRepository.save(user).getId();
    }

    public String validateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("사용할 수 없는 이메일입니다.");
        }
        return email;
    }

    public String validateNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("사용할 수 없는 닉네임입니다.");
        }
        return nickname;
    }

    public String sendAuthCodeMessage(String phoneNumber) {
        System.out.println("========== SMS 인증번호 전송 요청 ==========");
        System.out.println("수신 번호: " + phoneNumber);
        try {
            String authCode = getAuthCodeSmsResponse(phoneNumber);
            System.out.println("SMS 전송 성공! 인증번호: " + authCode);
            return authCode;
        } catch (Exception e) {
            System.err.println("SMS 전송 실패: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    private String getAuthCodeSmsResponse(String phoneNumber) {
        Random rand = new Random();
        String authCode = "";
        for (int i = 0; i < 6; i++) {
            String randomNumber = Integer.toString(rand.nextInt(10));
            authCode += randomNumber;
        }
        String text = ("[Toty] 인증번호[" + authCode + "]를 입력하세요.");
        Message message = createMessage(phoneNumber, text);

        System.out.println("인증번호 생성: " + authCode);
        System.out.println("메시지 내용: " + text);
        System.out.println("발신 번호: " + messageFrom);
        System.out.println("수신 번호: " + phoneNumber);

        redisService.setData(phoneNumber, authCode, Duration.ofMinutes(5));
        System.out.println("Redis에 인증번호 저장 완료 (5분 만료)");

        System.out.println("CoolSMS API 호출 중...");
        SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
        System.out.println("CoolSMS 응답 - 상태: " + response.getStatusCode());
        System.out.println("CoolSMS 응답 - 메시지 ID: " + response.getMessageId());

        return authCode;
    }

    private Message createMessage(String to, String text) {
        Message message = new Message();
        message.setFrom(messageFrom);
        message.setTo(to);
        message.setText(text);
        return message;
    }

    public boolean checkAuthCode(String phoneNumber, String authCode) {
        String redisAuthCode = redisService.getData(phoneNumber);
        if (redisAuthCode == null) {
            throw new IllegalArgumentException("인증번호가 만료되었습니다.");
        } else if (authCode.equals(redisAuthCode)) {
            return true;
        } else {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }
    }
}
