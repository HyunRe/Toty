package com.toty.user.application;

import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.roleRefreshScheduler.dto.UserIdAndRoleDto;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public void deleteUser(Long userId) {
        User foundUser = findById(userId);
        foundUser.deleteUser();
    }

    public User findById(Long userId) {
        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
        return foundUser;
    }

    @Transactional
    public void updateUserRole(UserIdAndRoleDto dto) {
        User foundUser = findById(dto.getId());
        foundUser.updateRole(dto.getRole());
    }

    public void registerUser(User user) {
        userRepository.save(user);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    // 이메일 찾기 (이름 + 전화번호로 조회)
    public String findEmailByUsernameAndPhoneNumber(String username, String phoneNumber) {
        User user = userRepository.findByUsernameAndPhoneNumber(username, phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("입력하신 정보와 일치하는 사용자를 찾을 수 없습니다."));
        return user.getEmail();
    }

    // 비밀번호 재설정 (이메일 + 이름 + 전화번호 인증 후 비밀번호 변경)
    @Transactional
    public void resetPassword(String email, String username, String phoneNumber, String newPassword) {
        User user = userRepository.findByEmailAndUsernameAndPhoneNumber(email, username, phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("입력하신 정보와 일치하는 사용자를 찾을 수 없습니다."));

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(encodedPassword);
    }
}