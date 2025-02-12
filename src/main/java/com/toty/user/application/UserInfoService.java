package com.toty.user.application;

import com.toty.common.domain.Tag;
import com.toty.following.domain.FollowingRepository;
import com.toty.user.domain.model.Site;
import com.toty.user.domain.model.User;
import com.toty.user.domain.model.UserLink;
import com.toty.user.domain.model.UserTag;
import com.toty.user.domain.repository.UserLinkRepository;
import com.toty.user.domain.repository.UserRepository;
import com.toty.user.domain.repository.UserTagRepository;
import com.toty.user.dto.request.BasicInfoUpdateRequest;
import com.toty.user.dto.request.LinkUpdateRequest;
import com.toty.user.dto.request.TagUpdateRequest;
import com.toty.user.dto.request.UserInfoUpdateRequest;
import com.toty.user.dto.response.LinkInfo;
import com.toty.user.dto.response.UserInfoResponse;
import jakarta.transaction.Transactional;
import java.util.Arrays;
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
    private final UserService userService;

    @Value("${user.img-path}")
    private String basePath;

    // 본인 확인
    private boolean isSelfAccount(User user, Long id){
        // id의 Null 여부는 presentation에서 검증 필요
        return user.getId().equals(id);
    }

    // 사용자 정보 전체 조회
    private UserInfoResponse getUserInfoByAccount(Long myId, Long targetId, boolean isOwner) {
        User foundUser = userRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        
        List<String> userTags = userTagRepository.findByUserId(targetId)
                .stream()
                .map(userTag -> userTag.getTag().getTag())
                .toList();
        List<LinkInfo> userLinks = userLinkRepository.findByUserId(targetId)
                .stream()
                .map(userLink -> new LinkInfo(userLink.getSite().getValue(), userLink.getUrl()))
                .toList();
        Long followingCount = followingRepository.countFollowingsByUserId(targetId);
        Long followerCount = followingRepository.countFollowersByUserId(targetId);

        return UserInfoResponse.builder()
                .id(foundUser.getId())
                .email(isOwner ? foundUser.getEmail() : null)
                .phoneNumber(isOwner ? foundUser.getPhoneNumber() : null)
                .nickname(foundUser.getNickname())
                .username(isOwner ? foundUser.getUsername() : null)
                .profileImgUrl(foundUser.getProfileImageUrl())
                .emailSubscribed(isOwner ? foundUser.getSubscribeInfo().isEmailSubscribed() : null)
                .smsSubscribed(isOwner ? foundUser.getSubscribeInfo().isSmsSubscribed() : null)
                .notificationAllowed(isOwner ? foundUser.getSubscribeInfo().isNotificationAllowed() : null)
                .tags(userTags)
                .links(userLinks)
                .followingCount(followingCount)
                .followerCount(followerCount)
                .role(foundUser.getRole())
                .isFollowing(!isOwner ? followingRepository.existsByFromUserIdAndToUserId(myId, targetId) : null)
                .createdAt(isOwner ? foundUser.getCreatedAt() : null)
                .build();
    }

    // 사용자 정보 조회
    public UserInfoResponse getUserInfo(User requester, Long targetId) {
        if (isSelfAccount(requester, targetId)) {
            return getUserInfoByAccount(requester.getId(), targetId, true);
        } else {
            return getUserInfoByAccount(requester.getId(), targetId, false);
        }
    }

    // 기본 정보 수정(닉네임, 사진, 구독)
    @Transactional
    public void updateUserBasicInfo(Long userId, BasicInfoUpdateRequest newInfo, MultipartFile imgFile) {
        User foundUser = userService.findById(userId);
        if (imgFile != null && !imgFile.isEmpty()) {
            try {
                String savePath = basePath + userId;
                String contentType = imgFile.getContentType().split("/")[1];
                String imgPath = savePath + "." + contentType;
                imgFile.transferTo(new File(imgPath)); // ex) --.jpg, --hi.png
                foundUser.updateprofileImg(imgPath);
            } catch (IOException e) {
                throw new RuntimeException();
                // todo 예외 시
                // throw new ExpectedException(ErrorCode.FileIOException);
            }
        }

        newInfo.getSubscriptionAllowed().forEach(alert -> {
            switch (alert) {
                case "email":
                    foundUser.updateEmailSubscription(true);
                    break;
                case "sms":
                    foundUser.updateSmsSubscription(true);
                    break;
                case "notification":
                    foundUser.updateNotificationAllowed(true);
                    break;
            }
        });

    }

    // link 변경
    @Transactional
    public void updateUserLinks(Long userId, LinkUpdateRequest request) {
        userLinkRepository.deleteByUserId(userId);
        request.getLinks().forEach(link -> {
            userLinkRepository.save(new UserLink(userService.findById(userId), siteStringToEnum(link.getSite()), link.getUrl()));
        });
    }

    //태그 선택
    public void updateUserTags(Long userId, TagUpdateRequest tags) {
        User foundUser = userService.findById(userId);
        userTagRepository.deleteByUserId(userId);
        tags.getTags().forEach(tag -> {
            userTagRepository.save(new UserTag(foundUser, tagStringToEnum(tag)));
        });
    }

    // 휴대폰 번호 변경
    public void updatePhoneNumber(Long userId, String phoneNumber) {
        User foundUser = userService.findById(userId);
        foundUser.updatePhoneNumber(phoneNumber);
    }

    // 사용 안함
    @Transactional
    public void updateUserInfo(Long userId, UserInfoUpdateRequest newInfo, MultipartFile imgFile) {
        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (imgFile != null && !imgFile.isEmpty()) {
            try {
                String savePath = basePath + userId;
                String contentType = imgFile.getContentType().split("/")[1];
                imgFile.transferTo(new File(savePath+"."+contentType)); // ex) --.jpg, --hi.png
                foundUser.updateInfo(newInfo, savePath);
            } catch (IOException e) {
                throw new RuntimeException();
                // todo 예외 시
                // throw new ExpectedException(ErrorCode.FileIOException);
            }
        }

        userTagRepository.deleteByUserId(userId);
        newInfo.getTags().forEach(tag -> {
            Tag tagEnum = tagStringToEnum(tag);
            userTagRepository.save(new UserTag(foundUser, tagEnum));
        });

        userLinkRepository.deleteByUserId(userId);
        newInfo.getLinks().forEach(link -> {
            userLinkRepository.save(new UserLink(foundUser, siteStringToEnum(link.getSite()), link.getUrl()));
        });
    }



    // 사용 보류
    public UserInfoResponse getMyInfoForUpdate(User user, Long id) {
        if (isSelfAccount(user, id)) {
            return getUserInfoByAccount(id, id,true);
        } else {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }
    }

    // 문자열-> Tag enum을 리턴
    private static Tag tagStringToEnum(String tagValue) {
        return Arrays.stream(Tag.values())
                .filter(t -> t.getTag().equalsIgnoreCase(tagValue))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid tag: " + tagValue));
    }

    private static Site siteStringToEnum(String siteValue) {
        return siteValue.equals("GITHUB") ? Site.GITHUB : Site.BLOG;
    }

}
