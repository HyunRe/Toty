package com.toty.page;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class PageController {

    // 기본 페이지
    @GetMapping("/home")
    public String home(){
        return "common/home"; // todo
    }

    // 메세지 띄우기
    @PostMapping("/alert")
    public String alert() {
        return "common/alertMsg";
    }

    // 로그인 성공 후 기본 리다이렉트
    @GetMapping("/login-success")
    public String loginSuccess(Model model) {
        model.addAttribute("msg", "환영합니다.");
        model.addAttribute("url", "/successUrl");
        return "common/alertMsg";
    }

    // 리프레시 토큰 만료 이후 재로그인
    @GetMapping("/login")
    public String loginPage() {
        return "common/home"; // todo
    }

}
