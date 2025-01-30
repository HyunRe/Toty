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
    public void updateUserInfo(Long id, UserInfoUpdateRequest newInfo, MultipartFile imgFile) {
        try {
            User foundUser = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

            // 서버에 이미지 저장 (원래 이미지가 있었다면 덮어쓰기)
            String name = imgFile.getOriginalFilename();
            System.out.println(imgFile.getOriginalFilename());
            String savePath = basePath + id;
            imgFile.transferTo(new File(savePath)); // throws IOException

            // user 정보 수정
            foundUser.updateInfo(newInfo,savePath);
            userRepository.save(foundUser);

            // tag와 links 저장
            userTagRepository.deleteByUserId(id);
            newInfo.getTags().stream().map(tag -> userTagRepository.save(new UserTag(foundUser, tag))); // 새 객체 생성

            userLinkRepository.deleteAllByUserId(id);
            newInfo.getLinks().stream().map(site -> userLinkRepository.save(new UserLink(foundUser, site.getSite(), site.getUrl())));
        } catch (Exception e){ // IOException
            // todo 예외 시
            // throw new ExpectedException(ErrorCode.FileIOException);
        }
    }

    public UserInfoResponse getMyInfoForUpdate(User user, Long id) {
        if (isSelfAccount(user, id)) {
            // 데이터 DTO에 담기(True)
            return getUserInfoByAccount(id,true); // email은 readonly로..
        } else {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }
    }

}
