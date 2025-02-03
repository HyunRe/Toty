package com.toty.user.presentation;

import com.toty.global.annotation.CurrentUser;
import com.toty.user.application.UserInfoService;
import com.toty.user.application.UserService;
import com.toty.user.application.UserSignUpService;
import com.toty.user.domain.model.User;
import com.toty.user.dto.request.UserInfoUpdateRequest;
import com.toty.user.dto.request.UserSignUpRequest;
import com.toty.user.dto.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

//
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;
    private final UserSignUpService userSignUpService;
    private final UserInfoService userInfoService;

    @GetMapping("/test")
    public ResponseEntity test(@CurrentUser User user) {
        System.out.println("arrive");
        return ResponseEntity.ok(user);
    }

    // 회원 가입
    @PostMapping("/signup")
    public ResponseEntity<Long> signUp(@RequestBody UserSignUpRequest userSignUpRequest) {
        Long userId = userSignUpService.signUp(userSignUpRequest);
        return new ResponseEntity<>(userId, HttpStatus.CREATED);
    }

    // 회원가입 - 이메일 중복 확인
    @GetMapping("/check-email")
    public ResponseEntity<String> emailValidation(@RequestParam(name = "email") String email) {
        String response = userSignUpService.validateEmail(email);
        return ResponseEntity.ok(response);
    }

    // 회원가입 - 닉네임 증복 확인
    @GetMapping("/check-nickname")
    public ResponseEntity<String> nicknameValidation(@RequestParam(name = "nickname") String nickname) {
        String response = userSignUpService.validateNickname(nickname);
        return ResponseEntity.ok(response);
    }

    // 회원 탈퇴
    @DeleteMapping("/")
    public ResponseEntity<String> delete(@CurrentUser User user) {
        userService.deleteUser(user.getId());
        return ResponseEntity.ok("true");
    }

    // 나의/상대방의 정보 보기
        // 본인인지 아닌지 확인 -> 아니면 약식 정보만 전달
    @GetMapping("/{id}/info")
    public ResponseEntity<UserInfoResponse> getUserInfo(@CurrentUser User user,
                                                        @PathVariable("id") Long id) {
        UserInfoResponse userInfo = userInfoService.getUserInfo(user, id);
        return ResponseEntity.ok(userInfo);
    }

    // 내 정보 수정
    @PatchMapping("/info")
    public ResponseEntity<String> updateUserInfo(@CurrentUser User user,
                                                 @RequestPart UserInfoUpdateRequest newInfo,
                                                 @RequestPart(required = false) MultipartFile imgFile) {
        userInfoService.updateUserInfo(user.getId(), newInfo, imgFile);
        return ResponseEntity.ok("true");
    }
}