package com.toty.user.application;

import com.toty.Tag;
import com.toty.following.domain.FollowingRepository;
import com.toty.user.domain.model.User;
import com.toty.user.domain.model.UserLink;
import com.toty.user.domain.model.UserTag;
import com.toty.user.domain.repository.UserLinkRepository;
import com.toty.user.domain.repository.UserRepository;
import com.toty.user.domain.repository.UserTagRepository;
import com.toty.user.dto.request.UserInfoUpdateRequest;
import com.toty.user.dto.response.UserInfoResponse;
import com.toty.user.dto.response.UserLinkInfo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserInfoService {

    private final UserRepository userRepository;
    private final UserTagRepository userTagRepository;
    private final UserLinkRepository userLinkRepository;
    private final FollowingRepository followingRepository;

    @Value("${user.img-path}")
    private String basePath;

    // 본인 확인
    private boolean isSelfAccount(User user, Long id){
        // id의 Null 여부는 presentation에서 검증 필요
        return user.getId().equals(id);
    }

    private UserInfoResponse getUserInfoByAccount(Long userId, boolean isOwner) {
        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        List<Tag> userTags = userTagRepository.findByUserId(userId)
                .stream()
                .map(userTag -> userTag.getTag())
                .toList();
        List<UserLinkInfo> userLinks = userLinkRepository.findByUserId(userId)
                .stream()
                .map(userLink -> new UserLinkInfo(userLink.getSite(), userLink.getUrl()))
                .toList();
        Long followingCount = followingRepository.countFollowingsByUserId(userId);
        Long followerCount = followingRepository.countFollowersByUserId(userId);

        return UserInfoResponse.builder()
                .email(isOwner ? foundUser.getEmail() : null)
                .phoneNumber(isOwner ? foundUser.getPhoneNumber() : null)
                .nickname(foundUser.getNickname())
                .profileImgUrl(foundUser.getProfileImageUrl())
                .emailSubscribed(isOwner ? foundUser.getSubscribeInfo().isEmailSubscribed() : null)
                .smsSubscribed(isOwner ? foundUser.getSubscribeInfo().isSmsSubscribed() : null)
                .tags(userTags)
                .links(userLinks)
                .followingCount(followingCount)
                .followerCount(followerCount)
                .build();
    }

    public UserInfoResponse getUserInfo(User user, Long id) {
        if (isSelfAccount(user, id)) {
            return getUserInfoByAccount(id, true);
        } else {
            return getUserInfoByAccount(id, false);
        }
    }

    @Transactional
    public void updateUserInfo(Long userId, UserInfoUpdateRequest newInfo, MultipartFile imgFile) {
        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (imgFile != null && !imgFile.isEmpty()) {
            try {
                String savePath = basePath + userId;
                imgFile.transferTo(new File(savePath));
                foundUser.updateInfo(newInfo, savePath);
            } catch (IOException e) {
                throw new RuntimeException();
                // todo 예외 시
                // throw new ExpectedException(ErrorCode.FileIOException);
            }
        }

        userTagRepository.deleteByUserId(userId);
        newInfo.getTags().forEach(tags -> {
            userTagRepository.save(new UserTag(foundUser, tags));
        });

        userLinkRepository.deleteByUserId(userId);
        newInfo.getLinks().forEach(links -> {
            userLinkRepository.save(new UserLink(foundUser, links.getSite(), links.getUrl()));
        });
    }

    public UserInfoResponse getMyInfoForUpdate(User user, Long id) {
        if (isSelfAccount(user, id)) {
            return getUserInfoByAccount(id,true);
        } else {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }
    }
}
