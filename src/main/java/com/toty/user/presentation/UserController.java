package com.toty.user.presentation;

import com.toty.user.application.UserService;
import com.toty.user.domain.User;
import com.toty.user.presentation.dto.request.UserSignUpRequest;
import com.toty.user.presentation.dto.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원 가입
    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<Long> signUp(@RequestBody UserSignUpRequest userSignUpRequest) {
        Long userId = userService.signUp(userSignUpRequest);
        return new ResponseEntity<>(userId, HttpStatus.CREATED);
    }

    // 내 정보 보기
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<UserInfoResponse> getUserInfo(@PathVariable("id") Long id) {
        UserInfoResponse userInfo = userService.getUserInfo(id);
        return ResponseEntity.ok(userInfo);
    }
}