package com.toty.springconfig.fcm.token;

import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FCmTokenService {
    private final UserRepository userRepository;
    private final FcmTokenRepository fcmTokenRepository;

    // 토큰 생성 및 업데이트
    @Transactional
    public FcmToken saveToken(Long userId, String token) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
        FcmToken fcmToken = fcmTokenRepository.findByUserId(userId).orElse(new FcmToken(user, token));
        fcmToken.updateToken(token);

        return  fcmTokenRepository.save(fcmToken);
    }
}
