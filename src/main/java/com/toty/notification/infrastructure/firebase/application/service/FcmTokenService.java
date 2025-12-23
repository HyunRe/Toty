package com.toty.notification.infrastructure.firebase.application.service;

import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.notification.infrastructure.firebase.domain.model.FcmToken;
import com.toty.notification.infrastructure.firebase.domain.repository.FcmTokenRepository;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmTokenService {
    private final UserRepository userRepository;
    private final FcmTokenRepository fcmTokenRepository;

    // FCM 토큰 저장 또는 업데이트
    @Transactional
    public void saveOrUpdateToken(Long userId, String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                throw new ExpectedException(ErrorCode.INVALID_FCM_TOKEN);
            }

            String trimmedToken = token.trim();

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));

            // 1. 동일 유저가 동일 토큰을 이미 가지고 있는지
            Optional<FcmToken> existingUserToken =
                    fcmTokenRepository.findByUserIdAndToken(userId, trimmedToken);

            if (existingUserToken.isPresent()) {
                FcmToken userToken = existingUserToken.get();

                if (userToken.isActive()) {
                    log.info("Token already active for user={}", userId);
                } else {
                    userToken.activate(trimmedToken);
                    log.info("Reactivated existing token for user={}", userId);
                }
                return;
            }

            // 2. 다른 유저가 해당 토큰을 가지고 있는지
            List<FcmToken> otherUserTokens = fcmTokenRepository.findByToken(trimmedToken);

            if (!otherUserTokens.isEmpty()) {
                for (FcmToken t : otherUserTokens) {
                    t.deactivate();
                }
                log.info("Deactivated token from {} other users", otherUserTokens.size());
            }

            // 3. 새로운 토큰 생성
            FcmToken newToken = new FcmToken(user, trimmedToken);
            newToken.activate(trimmedToken);

            fcmTokenRepository.save(newToken);
            log.info("Saved new FCM token for user={}", userId);

        } catch (ExpectedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to saveOrUpdateToken userId={}, token={}", userId, token, e);
            throw new ExpectedException(ErrorCode.FCM_TOKEN_SAVE_FAILED);
        }
    }

    // 특정 유저의 토큰 비활성화
    @Transactional
    public void deactivateToken(Long userId, String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                log.warn("Invalid token for user={} deactivation (null or empty)", userId);
                throw new ExpectedException(ErrorCode.INVALID_FCM_TOKEN);
            }

            String trimmedToken = token.trim();

            Optional<FcmToken> fcmTokenOpt =
                    fcmTokenRepository.findByUserIdAndToken(userId, trimmedToken);

            if (fcmTokenOpt.isEmpty()) {
                log.warn("Token not found for user={} token={}", userId, trimmedToken);
                throw new ExpectedException(ErrorCode.FCM_TOKEN_NOT_FOUND);
            }

            FcmToken fcmToken = fcmTokenOpt.get();
            fcmToken.deactivate();
            log.info("Deactivated token for user={}", userId);

        } catch (ExpectedException e) {
            throw e; // 커스텀 예외는 그대로 던짐
        } catch (Exception e) {
            log.error("Unexpected error while deactivating token for user={}", userId, e);
            throw new ExpectedException(ErrorCode.FCM_TOKEN_DEACTIVATE_FAILED);
        }
    }

    // 특정 유저의 활성 토큰 조회
    @Transactional(readOnly = true)
    public List<String> getActiveTokensByUserId(Long userId) {
        try {
            List<String> tokens = fcmTokenRepository.findActiveTokensByUserId(userId);
            return tokens != null ? tokens : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to load active tokens for user={}", userId, e);
            throw new ExpectedException(ErrorCode.FCM_TOKEN_QUERY_FAILED);
        }
    }

    // 여러 유저의 활성 토큰 조회 (단체 알림용)
    @Transactional(readOnly = true)
    public List<String> getActiveTokensByUserIds(List<Long> userIds) {
        try {
            if (userIds == null || userIds.isEmpty()) {
                return Collections.emptyList();
            }

            List<String> tokens = fcmTokenRepository.findActiveTokensByUserIds(userIds);
            return tokens != null ? tokens : Collections.emptyList();

        } catch (Exception e) {
            log.error("Failed to load active tokens for users={}", userIds, e);
            throw new ExpectedException(ErrorCode.FCM_TOKEN_QUERY_FAILED);
        }
    }

    // 만료된 토큰들을 비활성화 (FCM에서 반환한 invalid tokens 처리용)
    @Transactional
    public void deactivateInvalidTokens(List<String> invalidTokens) {
        try {

            if (invalidTokens == null || invalidTokens.isEmpty()) {
                return;
            }

            int deactivatedCount = 0;

            for (String token : invalidTokens) {

                if (token == null || token.trim().isEmpty()) {
                    continue;
                }

                String trimmedToken = token.trim();

                List<FcmToken> fcmTokens = fcmTokenRepository.findByToken(trimmedToken);

                for (FcmToken fcmToken : fcmTokens) {
                    if (fcmToken.isActive()) {
                        fcmToken.deactivate();
                        deactivatedCount++;
                    }
                }

                if (!fcmTokens.isEmpty()) {
                    fcmTokenRepository.saveAll(fcmTokens);
                }
            }

            if (deactivatedCount > 0) {
                log.info("[FCM TOKENS DEACTIVATED] count={}", deactivatedCount);
            }

        } catch (Exception e) {
            log.error("Failed to deactivateInvalidTokens", e);
            throw new ExpectedException(ErrorCode.FCM_TOKEN_DEACTIVATE_FAILED);
        }
    }

    /**
     * 비활성화된 FCM 토큰들을 물리적으로 삭제 (스케줄러에서 호출)
     * @return 삭제된 토큰 개수
     */
    @Transactional
    public int deleteInactiveTokens() {
        try {
            long before = fcmTokenRepository.count();
            fcmTokenRepository.deleteByIsActiveFalse();
            long after = fcmTokenRepository.count();

            int deleted = (int) (before - after);
            log.info("[FCM TOKENS REMOVED] {}", deleted);
            return deleted;

        } catch (Exception e) {
            log.error("Failed to deleteInactiveTokens", e);
            throw new ExpectedException(ErrorCode.FCM_TOKEN_DELETE_FAILED);
        }
    }
}
