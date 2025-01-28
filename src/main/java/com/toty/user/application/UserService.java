package com.toty.user.application;

import com.toty.Tag;
import com.toty.following.domain.FollowingRepository;
import com.toty.user.domain.LoginProvider;
import com.toty.user.domain.User;
import com.toty.user.domain.UserLink;
import com.toty.user.domain.UserLinkRepository;
import com.toty.user.domain.UserRepository;
import com.toty.user.domain.UserTag;
import com.toty.user.domain.UserTagRepository;
import com.toty.user.presentation.dto.LinkDto;
import com.toty.user.presentation.dto.request.UserInfoUpdateRequest;
import com.toty.user.presentation.dto.request.UserSignUpRequest;
import com.toty.user.presentation.dto.response.UserInfoResponse;
import jakarta.transaction.Transactional;
import java.io.File;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserTagRepository userTagRepository;
    private final UserLinkRepository userLinkRepository;
    private final FollowingRepository followingRepository;

    @Value("${user.img-path}")
    private String basePath;

    public Long signUp(UserSignUpRequest userSignUpRequest) {
        if(userRepository.findByEmail(userSignUpRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 사용자입니다.");
        }
        String hashedPwd = BCrypt.hashpw(userSignUpRequest.getPassword(), BCrypt.gensalt());

        User user = User.builder()
                .email(userSignUpRequest.getEmail())
                .password(hashedPwd)
                .nickname(userSignUpRequest.getNickname())
                .phoneNumber(userSignUpRequest.getPhoneNumber())
                .loginProvider(LoginProvider.FORM)
                .build();
        return userRepository.save(user).getId();
    }

    // 본인 확인
    public boolean isSelfAccount(User user, Long id){
        if (id == user.getId()) {
            return true;
        }
        return false;
    }


    public UserInfoResponse getUserInfoResponse(User user, Long id) {
        UserInfoResponse userInfo;
        if (isSelfAccount(user, id)) {
            return userInfo = getUserInfo(id, true);
        } else {
            return userInfo = getUserInfo(id, false);
        }
    }

    public UserInfoResponse getUserInfo(Long userId, boolean isOwner) {
        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        List<Tag> userTags = userTagRepository.findByUserId(userId).stream().map(userTag -> userTag.getTag()).toList();
        List<LinkDto> userLinks = userLinkRepository.findAllByUserId(userId).stream().map(userLink -> new LinkDto(userLink.getSite(), userLink.getUrl())).toList();

        UserInfoResponse infoDto = UserInfoResponse.builder()
                .nickname(foundUser.getNickname())
                .tags(userTags)
                .profileImgUrl(foundUser.getProfileImageUrl())
                .links(userLinks)
                .followingCount(followingRepository.countFollowingsByUserId(userId))
                .followerCount(followingRepository.countFollowersByUserId(userId))
                .email(isOwner ? foundUser.getEmail() : null)
                .phoneNumber(isOwner ? foundUser.getPhoneNumber() : null)
                .emailSubscribed(isOwner ? foundUser.getSubscribeInfo().isEmailSubscribed() : null)
                .smsSubscribed(isOwner ? foundUser.getSubscribeInfo().isSmsSubscribed() : null)
                .build();
        return infoDto;
    }

    public UserInfoResponse getMyInfoForUpdate(User user, Long id) {
        if (isSelfAccount(user, id)) {
            // 데이터 DTO에 담기(True)
            return getUserInfo(id,true); // email은 readonly로..
        } else {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }
    }

    @Transactional
    public void updateUser(Long id, UserInfoUpdateRequest newInfo, MultipartFile imgFile) {
        try {
            User foundUser = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

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

    public String validateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("사용할 수 없는 이메일입니다.");
        }
        return email;
    }

    public String validateNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("사용할 수 없는 이메일입니다.");
        }
        return nickname;
    }


    public void deleteUser(User user, Long id) { // soft delete
        User foundUser = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        if (isSelfAccount(user, id)) {
            userRepository.softDeleteById(id); // Error catch..?
        } else {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }
    }
}
