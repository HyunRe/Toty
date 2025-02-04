package com.toty.user.application;

import com.toty.springconfig.redis.RedisService;
import com.toty.user.domain.model.SubscribeInfo;
import com.toty.user.dto.response.SmsAuthCodeResponse;
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
import org.jetbrains.annotations.NotNull;
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

    @PostConstruct
    public void initMessageService() {
        this.messageService = NurigoApp.INSTANCE.initialize(
                "NCSTN8AA40XJD2G1",
                "V6JX2HOLWB6PRBUJVEIA2E0YZZUUARBT",
                "https://api.coolsms.co.kr"
        );
    }

    @Value("${spring.coolsms.api.fromnumber}")
    private String messageFrom;

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
        try {
            String authCode = getAuthCodeSmsResponse(phoneNumber);
            return authCode;
        } catch (Exception e) {
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

        redisService.setData(phoneNumber, authCode, Duration.ofMinutes(5));

        SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
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
        return authCode.equals(redisAuthCode);
    }
}
