package com.toty.following.application;

import com.toty.following.domain.Following;
import com.toty.following.domain.FollowingRepository;
import com.toty.following.dto.response.FollowingListResponse;
import com.toty.following.dto.response.FollowingListResponse.PageInfo;
import com.toty.following.dto.response.FollowingListResponse.Summary;
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
public class FollowService {

    private final UserRepository userRepository;
    private final FollowingRepository followingRepository;

    public static final int PAGE_SIZE = 20;

    public Long follow(Long fromId, Long toId) {
        //todo 본인 확인

        User fromUser = userRepository.findById(fromId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        User toUser = userRepository.findById(toId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        followingRepository.save(new Following(fromUser, toUser));
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

