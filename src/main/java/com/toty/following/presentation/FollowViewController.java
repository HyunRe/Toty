package com.toty.following.presentation;

import com.toty.common.annotation.CurrentUser;
import com.toty.following.application.FollowService;
import com.toty.following.dto.request.FollowingRequest;
import com.toty.following.dto.response.FollowingListResponse;
import com.toty.user.domain.model.User;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/view/follow")
@Controller
@RequiredArgsConstructor
public class FollowViewController {

    private final FollowService followService;

    // 팔로워 목록 조회
    @GetMapping("/{uid}/followers")
    public String followersList(@CurrentUser User user,
                                @PathVariable("uid") Long id,
                                @RequestParam(value = "p", defaultValue = "1") int page,
                                Model model) {
        FollowingListResponse response = followService.pagedFollowings(id, true, page, user.getId());
        model.addAttribute("followers", response);
        model.addAttribute("data-type", "followers");
        return "user/follow";
    }

    // 팔로잉 목록 조회
    @GetMapping("/{uid}/followings")
    public String followingList(@CurrentUser User user,
                                @PathVariable("uid") Long id,
                                @RequestParam(value = "p", defaultValue = "1") int page,
                                Model model) {
        FollowingListResponse response = followService.pagedFollowings(id, false, page, user.getId());
        model.addAttribute("followings", response);
        model.addAttribute("data-type", "followings");
        return "user/follow";
    }
}
