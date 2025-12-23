package com.toty.roleRefreshScheduler.application;

import com.toty.common.baseException.NotificationSendException;
import com.toty.following.application.service.FollowingService;

import com.toty.notification.application.service.NotificationSendService;
import com.toty.notification.domain.type.EventType;
import com.toty.notification.dto.request.NotificationSendRequest;
import com.toty.roleRefreshScheduler.dto.UserIdAndRoleDto;
import com.toty.user.application.UserService;
import com.toty.user.domain.model.Role;
import com.toty.user.domain.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleRefreshScheduler {
    private final UserRepository userRepository;
    private final UserService userService;
    private final FollowingService followingService;
    private final NotificationSendService notificationSendService;
    private static final long MINIMUM_MENTOR_FOLLOWERS = 100;
    public static final Long SYSTEM_SENDER_ID = -1L;

    //멘토 선정
    @Scheduled(cron = "0 0 0 1 * ?") // 매월 1일 자정 수행(초 분 시간 일 월 요일)
    public void refreshRole() {
        List<UserIdAndRoleDto> users = userRepository.findAllByIsDeletedFalse();

        users.forEach(user -> {
            Long userId = user.getId();
            Role desiredRole = isQualified(userId) ? Role.MENTOR : Role.USER;
            if (user.getRole() != desiredRole) {
                userService.updateUserRole(new UserIdAndRoleDto(user.getId(), desiredRole));

                // 알림 전송
                sendMentorRoleChangeNotification(userId, desiredRole);
            }
        });
    }

    private boolean isQualified(Long id) {
        return followingService.countFollowers(id) >= MINIMUM_MENTOR_FOLLOWERS;
    }

    private void sendMentorRoleChangeNotification(Long userId, Role desiredRole) {
        EventType eventType = (desiredRole == Role.MENTOR) ? EventType.BECOME_MENTOR : EventType.REVOKE_MENTOR;
        NotificationSendRequest notificationSendRequest = new NotificationSendRequest(
                userId,                         // 알림 받을 사람
                SYSTEM_SENDER_ID,               // 시스템 발신자
                "시스템",                         // 시스템 발신자
                eventType,                      // 이벤트 유형
                String.valueOf(userId),         // 멘토 자격이 변경된 유저 ID
                false                           // RedisSubscriber에서 온 게 아님
        );

        try {
            notificationSendService.sendNotification(notificationSendRequest);
        } catch (Exception e) {
            throw new NotificationSendException(e);
        }
    }
}
