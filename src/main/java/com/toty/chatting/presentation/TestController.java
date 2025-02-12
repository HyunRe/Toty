package com.toty.chatting.presentation;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.stream.Collectors;

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

    @GetMapping("/check-role")
    @ResponseBody
    public String checkUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return "No authentication found";
        }
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));
        return "User Roles: " + roles;
    }
}
