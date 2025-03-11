package com.toty.user.presentation;

import com.toty.base.response.SuccessResponse;
import com.toty.common.annotation.CurrentUser;
import com.toty.springconfig.security.jwt.JwtTokenUtil;
import com.toty.user.application.UserInfoService;
import com.toty.user.application.UserService;
import com.toty.user.application.UserSignUpService;
import com.toty.user.domain.model.User;
import com.toty.user.dto.LinkUpdateDto;
import com.toty.user.dto.request.BasicInfoUpdateRequest;
import com.toty.user.dto.request.PhoneNumberUpdateRequest;
import com.toty.user.dto.TagUpdateDto;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;
    private final UserSignUpService userSignUpService;
    private final UserInfoService userInfoService;
    private final JwtTokenUtil jwtTokenUtil;

    // 회원가입 - 이메일 중복 확인
    @GetMapping("/test")
    public ResponseEntity testEndpoint(@CurrentUser User user) {
        return ResponseEntity.ok(user);
    }

    // 회원가입 - 이메일 중복 확인
    @GetMapping("/check-email")
    public ResponseEntity emailValidation(@RequestParam(name = "email") String email) {
        String response = userSignUpService.validateEmail(email);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "사용할 수 있는 이메일입니다.",
                response
        );
        return ResponseEntity.ok(successResponse);
    }

    // 회원가입 - 닉네임 증복 확인
    @GetMapping("/check-nickname")
    public ResponseEntity nicknameValidation(@RequestParam(name = "nickname") String nickname) {
        String response = userSignUpService.validateNickname(nickname);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "사용할 수 있는 닉네임입니다.",
                response
        );

        return ResponseEntity.ok(successResponse);
    }

    // 회원가입 - 휴대폰 인증번호 요청
    @PostMapping("/authCode")
    public ResponseEntity sendAuthCode(@RequestParam(name = "phoneNumber") String phoneNumber) {
        String response = userSignUpService.sendAuthCodeMessage(phoneNumber);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "인증번호가 전송되었습니다.",
                null
        );
        return ResponseEntity.ok(successResponse);
    }

    // 회원가입 - 휴대폰 인증번호 확인
    @PostMapping("/check-authCode")
    public ResponseEntity checkAuthCode(@RequestParam(name = "authCode") String authCode,
            @RequestParam(name = "phoneNumber") String phoneNumber) {
        Boolean response = userSignUpService.checkAuthCode(phoneNumber, authCode);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "인증되었습니다.",
                response
        );
        return ResponseEntity.ok(successResponse);
    }


    // 회원 탈퇴
    @DeleteMapping("/")
    public ResponseEntity delete(@CurrentUser User user, HttpServletResponse response) {
        response.addCookie(jwtTokenUtil.accessTokenRemover());
        userService.deleteUser(user.getId());
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "회원 탈퇴 성공",
                null
        );
        return ResponseEntity.ok(successResponse);
    }

    // 내 기본 정보 수정(닉네임, 프로필 사진)
    @PostMapping("/update")
    public ResponseEntity updateUserBasicInfo(@CurrentUser User user,
            @RequestBody BasicInfoUpdateRequest newInfo,
            @RequestPart(required = false) MultipartFile imgFile) {
        userInfoService.updateUserBasicInfo(user, user.getId(), newInfo, imgFile);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "정보가 수정되었습니다.",
                null
        );
        return ResponseEntity.ok(successResponse);
    }

    // 업데이트 창 (링크)
    @GetMapping("/updateLink")
    public ResponseEntity getUpdateLink(@CurrentUser User user, Model model) {
        return ResponseEntity.ok(userInfoService.getUserLinks(user.getId()));
    }

    // 내 링크 수정
    @PutMapping("/links")
    public ResponseEntity updateUserLinks(@CurrentUser User user,
            @RequestBody LinkUpdateDto request) {
        userInfoService.updateUserLinks(user, request);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "링크가 수정되었습니다.",
                null
        );
        return ResponseEntity.ok(successResponse);
    }

    /// 업데이트 창 (태그)
    @GetMapping("/updateTag")
    public ResponseEntity getUpdateTag(@CurrentUser User user, Model model) {
        return ResponseEntity.ok(userInfoService.getUserTags(user.getId()));
    }

    // 내 태그 수정
    @PutMapping("/tags")
    public ResponseEntity updateUserTags(@CurrentUser User user,
            @RequestBody TagUpdateDto dto) {
        userInfoService.updateUserTags(user, dto);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "태그가 수정되었습니다.",
                null
        );
        return ResponseEntity.ok(successResponse);
    }

    @PostMapping("/phone-number")
    public ResponseEntity updateUserPhoneNumber(@CurrentUser User user,
            @RequestBody PhoneNumberUpdateRequest request) {
        userInfoService.updatePhoneNumber(user, user.getId(), request);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "번호가 수정되었습니다.",
                null
        );
        return ResponseEntity.ok(successResponse);
    }

    @PostMapping("/status-message")
    public ResponseEntity updateUserStatusMessage(@CurrentUser User user,
            @RequestBody Map<String, String> request) {
        String statusMessage = request.get("statusMessage");
        userInfoService.updateUserStatusMessage(user, user.getId(), statusMessage);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "상태메시지가 수정되었습니다.",
                null
        );
        return ResponseEntity.ok(successResponse);
    }


}