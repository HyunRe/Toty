package com.toty.page.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class PageController {

    @GetMapping("/home")
    public String home(){
        return "home";
    }

    @GetMapping("/signup")
    public String signUpProc(){
        return "home";
    }

    // 로그인 오류 시 메세지 띄우기
    @PostMapping("/alert")
    public String loginError() {
        return "common/alertMsg";
    }

}
