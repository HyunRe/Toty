package com.toty.following.presentation;

import com.toty.annotation.CurrentUser;
import com.toty.following.application.FollowService;
import com.toty.following.domain.FollowingRepository;
import com.toty.following.presentation.dto.request.FollowingRequest;
import com.toty.following.presentation.dto.response.FollowingListResponse;
import com.toty.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/api/follow")
@Controller
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    // 팔로우하기
    @PostMapping("/")
    @ResponseBody
    public ResponseEntity follow (@CurrentUser User user, @RequestBody FollowingRequest followingRequest) {
        Long response = followService.follow(user.getId(), followingRequest.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 언팔로우하기
    @DeleteMapping("/")
    @ResponseBody
    public ResponseEntity unfollow (@CurrentUser User user,@RequestBody FollowingRequest followingRequest) {
        Long response = followService.unfollow(user.getId(), followingRequest.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
