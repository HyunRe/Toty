package com.toty.user.presentation.api;

import com.toty.common.response.SuccessResponse;
import com.toty.common.annotation.CurrentUser;
import com.toty.common.security.jwt.JwtTokenUtil;
import com.toty.user.application.UserInfoService;
import com.toty.user.application.UserService;
import com.toty.user.application.UserSignUpService;
import com.toty.user.domain.model.User;
import com.toty.user.dto.request.LinkUpdateDto;
import com.toty.user.dto.request.BasicInfoUpdateRequest;
import com.toty.user.dto.request.PhoneNumberUpdateRequest;
import com.toty.user.dto.request.TagUpdateDto;
import com.toty.user.dto.request.FindEmailRequest;
import com.toty.user.dto.request.ResetPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserApiController {
    private final UserService userService;
    private final UserSignUpService userSignUpService;
    private final UserInfoService userInfoService;
    private final JwtTokenUtil jwtTokenUtil;

    @Operation(summary = "테스트 엔드포인트", description = "[테스트용] 현재 로그인한 사용자 정보를 반환합니다")
    @GetMapping("/test")
    public ResponseEntity testEndpoint(@CurrentUser User user) {
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "이메일 중복 확인", description = "회원가입 시 이메일 사용 가능 여부를 확인합니다")
    @GetMapping("/check-email")
    public ResponseEntity emailValidation(@RequestParam(name = "email") String email) {
        System.out.println("========== 이메일 중복 확인 API 호출 ==========");
        System.out.println("요청 이메일: " + email);
        try {
            String response = userSignUpService.validateEmail(email);
            System.out.println("이메일 사용 가능: " + email);
            SuccessResponse successResponse = new SuccessResponse(
                    HttpStatus.OK.value(),
                    "사용할 수 있는 이메일입니다.",
                    response
            );
            return ResponseEntity.ok(successResponse);
        } catch (IllegalArgumentException e) {
            System.err.println("이메일 중복: " + email + " - " + e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "닉네임 중복 확인", description = "회원가입 시 닉네임 사용 가능 여부를 확인합니다")
    @GetMapping("/check-nickname")
    public ResponseEntity nicknameValidation(@RequestParam(name = "nickname") String nickname) {
        System.out.println("========== 닉네임 중복 확인 API 호출 ==========");
        System.out.println("요청 닉네임: " + nickname);
        try {
            String response = userSignUpService.validateNickname(nickname);
            System.out.println("닉네임 사용 가능: " + nickname);
            SuccessResponse successResponse = new SuccessResponse(
                    HttpStatus.OK.value(),
                    "사용할 수 있는 닉네임입니다.",
                    response
            );
            return ResponseEntity.ok(successResponse);
        } catch (IllegalArgumentException e) {
            System.err.println("닉네임 중복: " + nickname + " - " + e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "휴대폰 인증번호 요청", description = "회원가입 시 휴대폰 번호로 인증번호를 전송합니다")
    @PostMapping("/authCode")
    public ResponseEntity sendAuthCode(@RequestParam(name = "phoneNumber") String phoneNumber) {
        System.out.println("========== SMS 인증번호 요청 API 호출 ==========");
        System.out.println("요청 전화번호: " + phoneNumber);
        try {
            String response = userSignUpService.sendAuthCodeMessage(phoneNumber);
            System.out.println("SMS 인증번호 전송 성공!");
            SuccessResponse successResponse = new SuccessResponse(
                    HttpStatus.OK.value(),
                    "인증번호가 전송되었습니다.",
                    null
            );
            return ResponseEntity.ok(successResponse);
        } catch (Exception e) {
            System.err.println("SMS 인증번호 전송 실패: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Operation(summary = "휴대폰 인증번호 확인", description = "전송된 인증번호의 유효성을 확인합니다")
    @PostMapping("/check-authCode")
    public ResponseEntity checkAuthCode(@RequestParam(name = "authCode") String authCode,
            @RequestParam(name = "phoneNumber") String phoneNumber) {
        System.out.println("========== SMS 인증번호 확인 API 호출 ==========");
        System.out.println("전화번호: " + phoneNumber + ", 인증번호: " + authCode);
        try {
            Boolean response = userSignUpService.checkAuthCode(phoneNumber, authCode);
            System.out.println("SMS 인증번호 확인 성공!");
            SuccessResponse successResponse = new SuccessResponse(
                    HttpStatus.OK.value(),
                    "인증되었습니다.",
                    response
            );
            return ResponseEntity.ok(successResponse);
        } catch (IllegalArgumentException e) {
            System.err.println("SMS 인증번호 확인 실패: " + e.getMessage());
            throw e;
        }
    }


    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 계정을 탈퇴 처리합니다")
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

    @Operation(summary = "기본 정보 수정", description = "닉네임과 프로필 사진을 수정합니다")
    @PostMapping("/update")
    public ResponseEntity updateUserBasicInfo(@CurrentUser User user,
                                              @RequestPart("basicInfo") BasicInfoUpdateRequest newInfo,
                                              @RequestPart(value = "profileImage", required = false) MultipartFile imgFile) {
        userInfoService.updateUserBasicInfo(user, user.getId(), newInfo, imgFile);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "정보가 수정되었습니다.",
                null
        );
        return ResponseEntity.ok(successResponse);
    }

    @Operation(summary = "내 링크 정보 조회", description = "현재 등록된 링크 정보를 조회합니다")
    @GetMapping("/updateLink")
    public ResponseEntity getUpdateLink(@CurrentUser User user, Model model) {
        return ResponseEntity.ok(userInfoService.getUserLinks(user.getId()));
    }

    @Operation(summary = "링크 수정", description = "사용자의 링크 정보를 수정합니다")
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

    @Operation(summary = "내 태그 정보 조회", description = "현재 등록된 태그 정보를 조회합니다")
    @GetMapping("/updateTag")
    public ResponseEntity getUpdateTag(@CurrentUser User user, Model model) {
        return ResponseEntity.ok(userInfoService.getUserTags(user.getId()));
    }

    @Operation(summary = "태그 수정", description = "사용자의 태그 정보를 수정합니다")
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

    @Operation(summary = "전화번호 수정", description = "사용자의 전화번호를 수정합니다")
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

    @Operation(summary = "상태메시지 수정", description = "사용자의 상태메시지를 수정합니다")
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

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다")
    @GetMapping("/me")
    public ResponseEntity getMyInfo(@CurrentUser User user) {
        var userInfo = userInfoService.getUserInfoByAccount(user.getId(), user.getId());
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "내 정보를 조회했습니다.",
                userInfo
        );
        return ResponseEntity.ok(successResponse);
    }

    @Operation(summary = "다른 사용자 정보 조회", description = "특정 사용자의 정보를 조회합니다")
    @GetMapping("/{id}")
    public ResponseEntity getUserInfo(@CurrentUser User user, @PathVariable Long id) {
        var userInfo = userInfoService.getUserInfoByAccount(user.getId(), id);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "사용자 정보를 조회했습니다.",
                userInfo
        );
        return ResponseEntity.ok(successResponse);
    }

    @Operation(summary = "이메일 찾기", description = "이름과 전화번호로 이메일을 찾습니다")
    @PostMapping("/find-email")
    public ResponseEntity findEmail(@RequestBody FindEmailRequest request) {
        String email = userService.findEmailByUsernameAndPhoneNumber(request.getUsername(), request.getPhoneNumber());
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "이메일을 찾았습니다.",
                Map.of("email", email)
        );
        return ResponseEntity.ok(successResponse);
    }

    @Operation(summary = "비밀번호 재설정", description = "이메일, 이름, 전화번호 인증 후 비밀번호를 재설정합니다")
    @PostMapping("/reset-password")
    public ResponseEntity resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(
                request.getEmail(),
                request.getUsername(),
                request.getPhoneNumber(),
                request.getNewPassword()
        );
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "비밀번호가 재설정되었습니다.",
                null
        );
        return ResponseEntity.ok(successResponse);
    }

}