package com.toty.user.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api")
public class PageController {

    @GetMapping("/home")
    public String returnHome() {
        // todo 토큰
        return "pages/home";
    }

    @GetMapping("/alert")
    public String loginError() {
        return "common/alertMsg";
    }
}
