package com.toty.following.presentation.view;

import com.toty.common.annotation.CurrentUser;
import com.toty.following.application.FollowingService;
import com.toty.following.dto.response.FollowingListResponse;
import com.toty.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class FollowingViewController {
    private final FollowingService followingService;

    // 팔로워 목록 조회 (특정 사용자)
    @GetMapping("/view/follow/{uid}/followers")
    public String followersList(@CurrentUser User user,
                                @PathVariable("uid") Long id,
                                @RequestParam(value = "p", defaultValue = "1") int page,
                                Model model) {
        FollowingListResponse response = followingService.pagedFollowings(id, true, page, user.getId());
        model.addAttribute("followers", response);
        model.addAttribute("data-type", "followers");
        return "user/follow";
    }

    // 팔로잉 목록 조회 (특정 사용자)
    @GetMapping("/view/follow/{uid}/followings")
    public String followingList(@CurrentUser User user,
                                @PathVariable("uid") Long id,
                                @RequestParam(value = "p", defaultValue = "1") int page,
                                Model model) {
        FollowingListResponse response = followingService.pagedFollowings(id, false, page, user.getId());
        model.addAttribute("followings", response);
        model.addAttribute("data-type", "followings");
        return "user/follow";
    }

    // 내 팔로워 목록 조회
    @GetMapping("/view/following/followerList")
    public String myFollowersList(@CurrentUser User user,
                                  @RequestParam(value = "p", defaultValue = "1") int page,
                                  Model model) {
        FollowingListResponse response = followingService.pagedFollowings(user.getId(), true, page, user.getId());
        model.addAttribute("followers", response);
        model.addAttribute("data-type", "followers");
        return "user/follow";
    }

    // 내 팔로잉 목록 조회
    @GetMapping("/view/following/followingList")
    public String myFollowingList(@CurrentUser User user,
                                  @RequestParam(value = "p", defaultValue = "1") int page,
                                  Model model) {
        FollowingListResponse response = followingService.pagedFollowings(user.getId(), false, page, user.getId());
        model.addAttribute("followings", response);
        model.addAttribute("data-type", "followings");
        return "user/follow";
    }
}
