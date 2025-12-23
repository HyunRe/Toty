package com.toty.user.application;

import com.toty.common.domain.Tag;
import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.following.domain.repository.FollowingRepository;
import com.toty.user.domain.model.Site;
import com.toty.user.domain.model.User;
import com.toty.user.domain.model.UserLink;
import com.toty.user.domain.model.UserTag;
import com.toty.user.domain.repository.UserLinkRepository;
import com.toty.user.domain.repository.UserRepository;
import com.toty.user.domain.repository.UserTagRepository;
import com.toty.user.dto.request.LinkUpdateDto;
import com.toty.user.dto.request.BasicInfoUpdateRequest;
import com.toty.user.dto.request.PhoneNumberUpdateRequest;
import com.toty.user.dto.request.TagUpdateDto;
import com.toty.user.dto.response.LinkDto;
import com.toty.user.dto.response.UserInfoResponse;
import jakarta.transaction.Transactional;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserInfoService {

    private final UserTagRepository userTagRepository;
    private final UserLinkRepository userLinkRepository;
    private final FollowingRepository followingRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${user.img-path}")
    private String basePath;

    // 본인 확인
    private boolean isNotOwner(User user, Long id){
        // id의 Null 여부는 presentation에서 검증 필요
        return !user.getId().equals(id);
    }

    // 사용자 정보 전체 조회
    public UserInfoResponse getUserInfoByAccount(Long myId, Long targetId) {
        User foundUser = userService.findById(targetId);

        List<String> userTags = userTagRepository.findByUserId(targetId)
                .stream()
                .map(userTag -> userTag.getTag().getTag())
                .toList();
        List<LinkDto> userLinks = userLinkRepository.findByUserId(targetId)
                .stream()
                .map(userLink -> new LinkDto(userLink.getSite().getValue(), userLink.getUrl()))
                .toList();
        Long followingCount = followingRepository.countFollowingsByUserId(targetId);
        Long followerCount = followingRepository.countFollowersByUserId(targetId);

        boolean isOwner = myId.equals(targetId);

        return UserInfoResponse.builder()
                .id(foundUser.getId())
                .email(isOwner ? foundUser.getEmail() : null)
                .phoneNumber(isOwner ? foundUser.getPhoneNumber() : null)
                .nickname(foundUser.getNickname())
                .username(isOwner ? foundUser.getUsername() : null)
                .profileImgUrl(foundUser.getProfileImageUrl())
                .emailSubscribed(isOwner ? foundUser.getUserSubscribeInfo().isEmailSubscribed() : false)
                .smsSubscribed(isOwner ? foundUser.getUserSubscribeInfo().isSmsSubscribed() : false)
                .notificationAllowed(isOwner ? foundUser.getUserSubscribeInfo().isNotificationAllowed() : false)
                .tags(userTags)
                .links(userLinks)
                .followingCount(followingCount)
                .followerCount(followerCount)
                .role(foundUser.getRole())
                .status_message(foundUser.getStatusMessage())
                .isFollowing(!isOwner ? followingRepository.existsByFromUserIdAndToUserId(myId, targetId) : false)
                .createdAt(isOwner ? foundUser.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null)
                .build();
    }


    // 기본 정보 수정(닉네임, 사진, 구독, 비밀번호)
    @Transactional
    public void updateUserBasicInfo(User user, Long userId, BasicInfoUpdateRequest newInfo, MultipartFile imgFile) {
        if (isNotOwner(user, userId)) {
            throw new ExpectedException(ErrorCode.INSUFFICIENT_PERMISSION);
        }
        User foundUser = userService.findById(userId);

        if (!newInfo.getNickname().isBlank()) {
            foundUser.updateNickname(newInfo.getNickname());
        }

        // 비밀번호 변경 로직
        if (newInfo.getCurrentPassword() != null && !newInfo.getCurrentPassword().isBlank()
                && newInfo.getNewPassword() != null && !newInfo.getNewPassword().isBlank()) {

            // 현재 비밀번호 확인
            if (!passwordEncoder.matches(newInfo.getCurrentPassword(), foundUser.getPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }

            // 새 비밀번호 암호화 및 업데이트
            String encodedNewPassword = passwordEncoder.encode(newInfo.getNewPassword());
            foundUser.updatePassword(encodedNewPassword);
        }

        if (imgFile != null && !imgFile.isEmpty()) {
            try {
                String savePath = basePath + userId;
                String contentType = imgFile.getContentType().split("/")[1];
                String imgPath = savePath + "." + contentType;
                imgFile.transferTo(new File(imgPath)); // ex) --.jpg, --hi.png
                foundUser.updateprofileImg(imgPath);
            } catch (IOException e) {
                 throw new ExpectedException(ErrorCode.PROFILE_IMAGE_SAVE_ERROR);
            }
        }

        // 수신 동의 초기화 (모두 false로)
        foundUser.updateEmailSubscription(false);
        foundUser.updateSmsSubscription(false);
        foundUser.updateNotificationAllowed(false);

        // 체크된 항목만 true로 설정
        if (newInfo.getSubscriptionAllowed() != null) {
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

    }

    // 링크 수정 페이지 조회
    public LinkUpdateDto getUserLinks(Long userId) {
        List<LinkDto> userLinks = userLinkRepository.findByUserId(userId)
                .stream()
                .map(userLink -> new LinkDto(userLink.getSite().getValue(), userLink.getUrl()))
                .toList();
        LinkUpdateDto linkUpdateDto = new LinkUpdateDto(userId, userLinks);
        return linkUpdateDto;
    }

    // 링크 수정
    @Transactional
    public void updateUserLinks(User user, LinkUpdateDto dto) {
        Long userId = user.getId();
        if (isNotOwner(user, dto.getId())) {
            throw new ExpectedException(ErrorCode.INSUFFICIENT_PERMISSION);
        }
        userLinkRepository.deleteByUserId(userId);
        dto.getLinks().forEach(link -> {
            userLinkRepository.save(new UserLink(userService.findById(userId), siteStringToEnum(link.getSite()), link.getUrl()));
        });
    }

    // 태그 수정 페이지 조회
    public TagUpdateDto getUserTags(Long userId) {
        List<String> userTags = userTagRepository.findByUserId(userId)
                .stream()
                .map(userTag -> userTag.getTag().name().toLowerCase().replace("_", "-"))
                .toList();
        TagUpdateDto tagUpdateDto = new TagUpdateDto(userId, userTags);
        return tagUpdateDto;
    }

    //태그 수정
    @Transactional
    public void updateUserTags(User user, TagUpdateDto dto) {
        Long userId = user.getId();
        if (isNotOwner(user, dto.getId())) {
            throw new ExpectedException(ErrorCode.INSUFFICIENT_PERMISSION);
        }

        if (dto.getTags().size() > 10) {
            throw new ExpectedException(ErrorCode.Tag_Limit_Exceeded_Error);
        }
        User foundUser = userService.findById(userId);
        userTagRepository.deleteByUserId(userId);
        dto.getTags().forEach(tag -> {
            userTagRepository.save(new UserTag(foundUser, tagStringToEnum(tag)));
        });
    }

    // 휴대폰 번호 변경
    public void updatePhoneNumber(User user, Long userId, PhoneNumberUpdateRequest phoneNumberDto) {
        if (isNotOwner(user, userId)) {
            throw new ExpectedException(ErrorCode.INSUFFICIENT_PERMISSION);
        }
        User foundUser = userService.findById(userId);
        foundUser.updatePhoneNumber(phoneNumberDto.getPhoneNumber());
    }

//    // 전체 정보 수정
//    @Transactional
//    public void updateUserInfo(User user, Long userId, UserInfoUpdateRequest newInfo, MultipartFile imgFile) {
//        if (isNotOwner(user, userId)) {
//            throw new ExpectedException(ErrorCode.INSUFFICIENT_PERMISSION);
//        }
//        User foundUser = userService.findById(userId);
//
//        if (imgFile != null && !imgFile.isEmpty()) {
//            try {
//                String savePath = basePath + userId;
//                String contentType = imgFile.getContentType().split("/")[1];
//                imgFile.transferTo(new File(savePath+"."+contentType)); // ex) --.jpg, --hi.png
//                foundUser.updateInfo(newInfo, savePath);
//            } catch (IOException e) {
//                throw new ExpectedException(ErrorCode.PROFILE_IMAGE_SAVE_ERROR);
//            }
//        }
//
//        userTagRepository.deleteByUserId(userId);
//        newInfo.getTags().forEach(tag -> {
//            Tag tagEnum = tagStringToEnum(tag);
//            userTagRepository.save(new UserTag(foundUser, tagEnum));
//        });
//
//        userLinkRepository.deleteByUserId(userId);
//        newInfo.getLinks().forEach(link -> {
//            userLinkRepository.save(new UserLink(foundUser, siteStringToEnum(link.getSite()), link.getUrl()));
//        });
//    }


    // 문자열-> Tag enum을 리턴
    private static Tag tagStringToEnum(String tagValue) {
        return Arrays.stream(Tag.values())
                .filter(t -> t.name().equalsIgnoreCase(tagValue.replace("-","_")))
                .findFirst().get();
    }

    private static Site siteStringToEnum(String siteValue) {
        return siteValue.toLowerCase().equals("github") ? Site.GITHUB : Site.BLOG;
    }

    public void updateUserStatusMessage(User user, Long id, String request) {
        if (isNotOwner(user, id)) {
            throw new ExpectedException(ErrorCode.INSUFFICIENT_PERMISSION);
        }
        user.saveStatusMessage(request);
        userRepository.save(user);
    }
}
