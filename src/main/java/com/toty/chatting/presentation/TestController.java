package com.toty.chatting.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/t12")
public class TestController {
    @ResponseBody
    @RequestMapping("/v")
    public String aa(@RequestParam("rid") long rid) {
        return "hello" + rid;
    }

    @RequestMapping("/l")
    public String aasa() {
        return "user/signIn";
    }
}
