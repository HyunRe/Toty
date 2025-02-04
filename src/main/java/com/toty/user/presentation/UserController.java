package com.toty.user.presentation;

import com.toty.user.application.UserService;
import com.toty.user.domain.model.User;
import com.toty.user.presentation.dto.request.UserSignUpRequest;
import com.toty.user.presentation.dto.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
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
    public User signUp(@RequestBody UserSignUpRequest userSignUpRequest) {
        return userService.signUp(userSignUpRequest);
    }

    // 내 정보 보기
    @GetMapping("/{id}")
    @ResponseBody
    public UserInfoResponse getUserInfo(@PathVariable("id") Long id) {
        return userService.getUserInfo(id);
    }
}