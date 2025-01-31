package com.toty.user.application;

import com.toty.user.domain.model.LoginProvider;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import com.toty.user.dto.request.UserSignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSignUpService {

    private final UserRepository userRepository;

    @Transactional
    public Long signUp(UserSignUpRequest userSignUpRequest) {
        if(userRepository.existsByEmail(userSignUpRequest.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 사용자입니다.");
        }

        String hashedPwd = BCrypt.hashpw(userSignUpRequest.getPassword(), BCrypt.gensalt());

        User user = User.builder()
                .email(userSignUpRequest.getEmail())
                .password(hashedPwd)
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
}
