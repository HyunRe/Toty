package com.toty.page.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
public class PageController {

    // 기본 페이지
    @GetMapping("/home")
    public String home(){
        return "home";
    }

    // 로그인 오류 시 메세지 띄우기
    @PostMapping("/alert")
    public String loginError() {
        return "common/alertMsg";
    }

    // 현재 /login 경로 security 기본 제공 페이지 사용 중
//    // 리프레시 토큰 만료 이후 재로그인
//    @GetMapping("/login")
//    public String loginPage() {
//        return "login";
//    }

}
