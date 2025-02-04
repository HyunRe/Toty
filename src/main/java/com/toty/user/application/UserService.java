package com.toty.user.application;

import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import com.toty.user.presentation.dto.request.UserSignUpRequest;
import com.toty.user.presentation.dto.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User signUp(UserSignUpRequest userSignUpRequest) {
        if(userRepository.findByEmail(userSignUpRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 사용자입니다.");
        }

        User newUser = new User(userSignUpRequest.getEmail(), userSignUpRequest.getPassword());
        return userRepository.save(newUser);
    }

    public UserInfoResponse getUserInfo(Long userId) {
        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return new UserInfoResponse(foundUser.getEmail());
    }
}
