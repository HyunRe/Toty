package com.toty.following.application.service;

import com.toty.common.baseException.NotificationSendException;
import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.following.domain.model.Following;
import com.toty.following.domain.repository.FollowingRepository;
import com.toty.following.dto.response.FollowingListResponse;
import com.toty.following.dto.response.FollowingListResponse.PageInfo;
import com.toty.following.dto.response.FollowingListResponse.Summary;
import com.toty.notification.application.service.NotificationSendService;
import com.toty.notification.domain.type.EventType;
import com.toty.notification.dto.request.NotificationSendRequest;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowingService {
    private final UserRepository userRepository;
    private final FollowingRepository followingRepository;
    private final NotificationSendService notificationSendService;

    public static final int PAGE_SIZE = 20;

    public Long follow(Long fromId, Long toId) {
        // 본인 팔로우 방지
        if (fromId.equals(toId)) {
            throw new ExpectedException(ErrorCode.CANNOT_FOLLOW_SELF);
        }

        // 중복 팔로우 방지
        if (followingRepository.existsByFromUserIdAndToUserId(fromId, toId)) {
            throw new IllegalArgumentException("이미 팔로우한 사용자입니다.");
        }

        User fromUser = userRepository.findById(fromId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        User toUser = userRepository.findById(toId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        followingRepository.save(new Following(fromUser, toUser));

        NotificationSendRequest notificationSendRequest = new NotificationSendRequest(
                toId,                       // 알림 받을 사람
                fromId,                     // 알림 보낸 사람
                fromUser.getNickname(),     // 알림 보낸 사람 닉네임
                EventType.FOLLOW,           // 알림 유형
                fromId.toString(),          // 팔로잉한 사용자 ID
                false                       // RedisSubscriber에서 온 게 아님
        );

        try {
            notificationSendService.sendNotification(notificationSendRequest);
        } catch (Exception e) {
            throw new NotificationSendException(e);
        }

        return toId;
    }

    public Long unfollow(Long fromId, Long toId) {
        boolean followingExist = followingRepository.existsByFromUserIdAndToUserId(fromId, toId);
        if (followingExist) {
            followingRepository.deleteById(
                    followingRepository.findByFromUserIdAndToUserId(fromId, toId).getId());
            return toId;
        } else {
            // todo 오류 수정
            throw new IllegalArgumentException("언팔로우 불가능");
        }
    }

    public FollowingListResponse pagedFollowings(Long userId, boolean isToUser,int page, Long myId) {
        // isToUser = True 나를 팔로우한 사람 리스트 가져오기 / False면 내가 팔로우하는 사람 리스트 가져오기
        Pageable pageable = PageRequest.of(page-1, PAGE_SIZE);
        Page<Following> followings;
        List<Summary> userSummaries;
        if (isToUser) { // userId를 팔로우 하는 사람
            followings = followingRepository.findPagedFollowingByToUserId(pageable, userId);
            userSummaries = followings.stream()
                    .map(following -> {
                        User fromUser = following.getFromUser();
                        return new Summary(
                                fromUser.getId(),
                                fromUser.getProfileImageUrl(),
                                fromUser.getNickname(),
                                followingRepository.existsByFromUserIdAndToUserId(myId, fromUser.getId())
                        );
                    })
                    .toList();
        } else { // userId가 팔로우 하는 사람
            followings = followingRepository.findPagedFollowingByFromUserId(pageable, userId);
            userSummaries = followings.stream()
                    .map(following -> {
                        User toUser = following.getToUser(); // 중복 호출 방지
                        return new Summary(
                                toUser.getId(),
                                toUser.getProfileImageUrl(),
                                toUser.getNickname(),
                                followingRepository.existsByFromUserIdAndToUserId(myId, toUser.getId())
                        );
                    })
                    .toList();
        }

        PageInfo pages = new PageInfo(page, PAGE_SIZE, followings.getTotalPages());

        FollowingListResponse response = new FollowingListResponse(userSummaries, pages);
        return response;
    }

    public Long countFollowers(Long userId) {
        return followingRepository.countFollowersByUserId(userId);
    }

    public Long countFollowings(Long userId) {
        return followingRepository.countFollowingsByUserId(userId);
    }
}

