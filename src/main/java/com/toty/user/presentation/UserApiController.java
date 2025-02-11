package com.toty.user.presentation;

import com.toty.base.response.SuccessResponse;
import com.toty.common.annotation.CurrentUser;
import com.toty.springconfig.security.jwt.JwtTokenUtil;
import com.toty.user.application.UserInfoService;
import com.toty.user.application.UserService;
import com.toty.user.application.UserSignUpService;
import com.toty.user.domain.model.User;
import com.toty.user.dto.request.UserInfoUpdateRequest;
import com.toty.user.dto.response.UserInfoResponse;
import jakarta.servlet.http.HttpServletResponse;
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
    private final JwtTokenUtil jwtTokenUtil;

    // 회원가입 - 이메일 중복 확인
    @GetMapping("/test")
    public ResponseEntity test(@CurrentUser User user) {
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
    public ResponseEntity checkAuthCode(@RequestParam(name = "authCode") String authCode, @RequestParam(name = "phoneNumber") String phoneNumber) {
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

    // 내 정보 수정
    @PatchMapping("/info")
    public ResponseEntity updateUserInfo(@CurrentUser User user,
                                                 @RequestPart UserInfoUpdateRequest newInfo,
                                                 @RequestPart(required = false) MultipartFile imgFile) {
        userInfoService.updateUserInfo(user.getId(), newInfo, imgFile);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "정보가 수정되었습니다.",
                null
        );
        return ResponseEntity.ok(successResponse);
    }

    // 상대방의 정보 보기
    // 본인인지 아닌지 확인 -> 아니면 약식 정보만 전달
    @GetMapping("/{id}/info") //
    public ResponseEntity getUserInfo(@CurrentUser User user,
            @PathVariable("id") Long id) {
        UserInfoResponse userInfo = userInfoService.getUserInfo(user, id);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "정보 조회에 성공했습니다.",
                userInfo
        );
        return ResponseEntity.ok(successResponse);
    }


}