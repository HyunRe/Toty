package com.toty.user.presentation;

import com.toty.global.annotation.CurrentUser;
import com.toty.user.application.UserInfoService;
import com.toty.user.application.UserSignUpService;
import com.toty.user.domain.model.User;
import com.toty.user.dto.request.UserSignUpRequest;
import com.toty.user.dto.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserViewController {

    private final UserSignUpService userSignUpService;
    private final UserInfoService userInfoService;

    @GetMapping("/signup")
    public String home(){
        return "user/signup";
    }

    // 회원 가입
    @PostMapping("/signup")
    public String signUp(@RequestBody UserSignUpRequest userSignUpRequest, Model model) {
        userSignUpService.signUp(userSignUpRequest);
        return "redirect:/home";
    }

    // 정보 수정(View)
    @GetMapping("/edit-form/{id}")
    public String updateProc(@CurrentUser User user, @PathVariable Long id, Model model){
        // 본인인지 확인 -> 아니면 예외

        UserInfoResponse userInfo = userInfoService.getMyInfoForUpdate(user, id);

        model.addAttribute("userInfo", userInfo);
        return "update";
    }
}
