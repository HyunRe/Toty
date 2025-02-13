package com.toty.user.presentation;

import com.toty.common.annotation.CurrentUser;
import com.toty.user.application.UserInfoService;
import com.toty.user.application.UserSignUpService;
import com.toty.user.domain.model.User;
import com.toty.user.dto.request.UserSignUpRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Map<String, String>> signUp (@RequestBody UserSignUpRequest userSignUpRequest, Model model) {
        userSignUpService.signUp(userSignUpRequest);
        Map<String, String> response = new HashMap<>();
        response.put("redirectUrl", "/view/users/home");

        return ResponseEntity.ok(response);
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
    @GetMapping("/alert/login-fail")
    public String loginFail(Model model) {
        model.addAttribute("msg", "로그인에 실패했습니다");
        model.addAttribute("url", "/view/users/login");
        return "common/alertMsg";
    }

    // 로그아웃 성공 안내 창
    @GetMapping("/alert/logout")
    public String logout(Model model) {
        model.addAttribute("msg", "로그아웃 되었습니다.");
        model.addAttribute("url", "/view/users/home");
        return "common/alertMsg";
    }


    // 업데이트 창 (내 정보 수정)
    @GetMapping("/updateInfo")
    public String getUpdateInfo(@CurrentUser User user, Model model) {
        model.addAttribute("userInfo", userInfoService.getUserInfoByAccount(user.getId(), user.getId()));
        return "user/updateInfo";
    }

    /// 업데이트 창 (링크)
    @GetMapping("/updateLink")
    public String getUpdateLink(@CurrentUser User user, Model model) {
        model.addAttribute("userInfo", user); // 링크로 수정 필요
        return "user/updateLink";
    }

    /// 업데이트 창 (휴대폰)
    @GetMapping("/updatePhone")
    public String getUpdatePhone(@CurrentUser User user, Model model) {
        return "user/updatePhone";
    }

    /// 업데이트 창 (태그)
    @GetMapping("/updateTag")
    public String getUpdateTag(@CurrentUser User user, Model model) {
        model.addAttribute("userInfo", user); // 태그로 수정 필요
        return "user/updateTag";
    }

}
