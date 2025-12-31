package com.toty.chat.application.service;

import com.toty.following.domain.model.Following;
import com.toty.following.domain.repository.FollowingRepository;
import com.toty.notification.application.service.NotificationSendService;
import com.toty.notification.domain.type.EventType;
import com.toty.notification.dto.request.NotificationSendRequest;
import com.toty.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 채팅방 알림 서비스
 * - 채팅방 생성 시 팔로워들에게 알림 전송
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomNotificationService {
    private final FollowingRepository followingRepository;
    private final NotificationSendService notificationSendService;

    /**
     * 채팅방 생성 알림을 팔로워들에게 전송
     * @param mentor 멘토 (채팅방 생성자)
     * @param roomId 생성된 채팅방 ID
     */
    public void notifyFollowersAboutNewChatRoom(User mentor, Long roomId) {
        List<Following> followings = followingRepository.findByToUserId(mentor.getId());
        log.info("멘토 {}의 팔로워 {}명에게 채팅방 생성 알림 전송 시작",
                mentor.getNickname(), followings.size());

        for (Following following : followings) {
            try {
                NotificationSendRequest notificationRequest = new NotificationSendRequest(
                        following.getFromUser().getId(),    // 알림 받을 사람 (팔로워)
                        mentor.getId(),                     // 알림 보낸 사람 (멘토)
                        mentor.getNickname(),               // 멘토 닉네임
                        EventType.MENTOR_CHAT,              // 이벤트 타입
                        String.valueOf(roomId),             // 채팅방 ID
                        false                               // RedisSubscriber에서 온 게 아님
                );

                notificationSendService.sendNotification(notificationRequest);
                log.debug("팔로워 {}에게 알림 전송 완료", following.getFromUser().getNickname());
            } catch (Exception e) {
                log.error("팔로워 {}에게 알림 전송 실패: {}",
                        following.getFromUser().getNickname(), e.getMessage(), e);
            }
        }

        log.info("멘토 {}의 팔로워들에게 채팅방 생성 알림 전송 완료", mentor.getNickname());
    }
}
