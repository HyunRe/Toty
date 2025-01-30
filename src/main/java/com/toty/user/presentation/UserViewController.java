package com.toty.user.presentation;

import com.toty.global.annotation.CurrentUser;
import com.toty.user.application.UserInfoService;
import com.toty.user.domain.model.User;
import com.toty.user.dto.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserViewController {

    private final UserInfoService userInfoService;

    // 정보 수정(View)
    @GetMapping("/edit-form/{id}")
    public String updateProc(@CurrentUser User user, @PathVariable Long id, Model model){
        // 본인인지 확인 -> 아니면 예외

        UserInfoResponse userInfo = userInfoService.getMyInfoForUpdate(user, id);

        model.addAttribute("userInfo", userInfo);
        return "update";
    }
}
