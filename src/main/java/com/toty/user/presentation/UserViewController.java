package com.toty.user.presentation;

import com.toty.annotation.CurrentUser;
import com.toty.user.application.UserService;
import com.toty.user.domain.User;
import com.toty.user.presentation.dto.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserViewController {

    private final UserService userService;

    // 정보 수정(View)
    @GetMapping("/edit-form/{id}")
    public String updateProc(@CurrentUser User user, @PathVariable Long id, Model model){
        // 본인인지 확인 -> 아니면 예외

        UserInfoResponse userInfo = userService.getMyInfoForUpdate(user, id);

        model.addAttribute("userInfo", userInfo);
        return "update";
    }

    // 회원 탈퇴(view)
    @DeleteMapping("/{id}")
    public String delete(@CurrentUser User user, @PathVariable Long id) {
        // 본인 확인 로직 -> 아니면 예외
        userService.deleteUser(user,id); //soft delete
        return "redirect:/api/home";
    }

}
