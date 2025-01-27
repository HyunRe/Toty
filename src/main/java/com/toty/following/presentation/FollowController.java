package com.toty.following.presentation;

import com.toty.following.application.FollowService;
import com.toty.following.domain.FollowingRepository;
import com.toty.following.presentation.dto.request.FollowingRequest;
import com.toty.following.presentation.dto.response.FollowingListResponse;
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

    @PostMapping("/")
    @ResponseBody
    public ResponseEntity follow (@RequestBody FollowingRequest followingRequest) {
        // 본인 정보 long
//        Long response = followService.follow("본인 id", followingRequest.getId());
        FollowingListResponse response = null;

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/")
    @ResponseBody
    public ResponseEntity unfollow (@RequestBody FollowingRequest followingRequest) {
        // 본인 정보 long
//        Long response = followService.unfollow("본인 id", followingRequest.getId());
        FollowingListResponse response = null;

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{uid}/followers")
    @ResponseBody
    public ResponseEntity<FollowingListResponse> followersList(@RequestParam(value = "p", defaultValue = "1") int page) {
//        FollowingListResponse response = followService.listFollowings("본인 id", true, page);
        FollowingListResponse response = null;

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{uid}/followings")
    @ResponseBody
    public ResponseEntity<FollowingListResponse> followingList(@RequestParam(value = "p", defaultValue = "1") int page) {
//        FollowingListResponse response = followService.listFollowings("본인 id", false, page);
        FollowingListResponse response = null;
        return ResponseEntity.ok(response);
    }
}
