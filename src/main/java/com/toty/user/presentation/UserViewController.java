package com.toty.user.presentation;

import com.toty.common.annotation.CurrentUser;
import com.toty.user.application.UserInfoService;
import com.toty.user.application.UserSignUpService;
import com.toty.user.domain.model.User;
import com.toty.user.dto.request.UserSignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/view/users")
@RequiredArgsConstructor
public class UserViewController {

    private final UserSignUpService userSignUpService;
    private final UserInfoService userInfoService;

    // 회원 가입 페이지 반환
    @GetMapping("/signup")
    public String signup(){
        return "user/signup";
    }

    // 회원 가입
    @PostMapping("/signup")
    public String signUp(@RequestBody UserSignUpRequest userSignUpRequest, Model model) {
        userSignUpService.signUp(userSignUpRequest);
        return "redirect:/view/users/home";
    }

    // 정보 수정(View)
    @GetMapping("/edit-form/{id}")
    public String updateProc(@CurrentUser User user, @PathVariable Long id, Model model){
        // 본인인지 확인 -> 아니면 예외
//
//        UserInfoResponse userInfo = userInfoService.getMyInfoForUpdate(user, id);

//        model.addAttribute("userInfo", userInfo);
        return "update";
    }

    // 기본 페이지
    @GetMapping("/home")
    public String home(){
        return "home";
    }

    // 리프레시 토큰 만료 이후 재로그인(액세스 토큰 유효성 검사x)
    @GetMapping("/login")
    public String loginPage() {
        return "common/login";
    }

    // 내 정보 조회
    @GetMapping("/info")
    public String getMyInfo(@CurrentUser User user, Model model) {
        model.addAttribute("userInfo", userInfoService.getUserInfoByAccount(user.getId(), user.getId()));
        return "user/detail";
    }

    //상대방의 정보 조회
    @GetMapping("/{id}/info")
    public String getUserInfo(@CurrentUser User user, @PathVariable("id") Long id, Model model) {
        model.addAttribute("userInfo", userInfoService.getUserInfoByAccount(user.getId(), id));
        return "user/info";
    }

    // 폼로그인 실패 창 띄우기
    @GetMapping("/login-fail")
    public String loginFail(Model model) {
        model.addAttribute("msg", "로그인에 실패했습니다");
        model.addAttribute("url", "/view/users/login");
        return "common/alertMsg";
    }
}
