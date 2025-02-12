package com.toty.user.application;

import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.roleRefreshScheduler.dto.UserIdAndRoleDto;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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

}