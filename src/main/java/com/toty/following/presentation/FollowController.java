package com.toty.following.presentation;

import com.toty.annotation.CurrentUser;
import com.toty.following.application.FollowService;
import com.toty.following.dto.request.FollowingRequest;
import com.toty.following.dto.response.FollowingListResponse;
import com.toty.user.domain.model.User;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity unfollow (@CurrentUser User user, @PathParam("id") Long id) {
        Long response = followService.unfollow(user.getId(), id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 팔로워 목록 조회
    @GetMapping("/{uid}/followers")
    public ResponseEntity<FollowingListResponse> followersList(@PathParam("uid") Long id, @RequestParam(value = "p", defaultValue = "1") int page) {
        FollowingListResponse response = followService.pagedFollowings(id, true, page);
        return ResponseEntity.ok(response);
    }

    // 팔로잉 목록 조회
    @GetMapping("/{uid}/followings")
    public ResponseEntity<FollowingListResponse> followingList(@PathParam("uid") Long id, @RequestParam(value = "p", defaultValue = "1") int page) {
        FollowingListResponse response = followService.pagedFollowings(id, false, page);
        return ResponseEntity.ok(response);
    }

}
