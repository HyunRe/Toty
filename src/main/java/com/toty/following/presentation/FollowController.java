package com.toty.following.presentation;

import com.toty.common.annotation.CurrentUser;
import com.toty.following.application.FollowService;
import com.toty.following.dto.request.FollowingRequest;
import com.toty.following.dto.response.FollowingListResponse;
import com.toty.user.domain.model.User;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/follow")
@RestController
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    // 팔로우하기
    @PostMapping("/")
    public ResponseEntity<Long> follow(@CurrentUser User user,
                                       @RequestBody FollowingRequest followingRequest) {
        Long response = followService.follow(user.getId(), followingRequest.getId());
        return ResponseEntity.ok(response);
    }

    // 언팔로우하기
    @DeleteMapping("/{id}")
    public ResponseEntity<Long> unfollow(@CurrentUser User user, @PathParam("id") Long id) {
        Long response = followService.unfollow(user.getId(), id);
        return ResponseEntity.ok(response);
    }

    // 팔로워 목록 조회
    @GetMapping("/{uid}/followers")
    public ResponseEntity<FollowingListResponse> followersList(@CurrentUser User user,
                                                               @PathParam("uid") Long id,
                                                               @RequestParam(value = "p", defaultValue = "1") int page) {
        FollowingListResponse response = followService.pagedFollowings(id, true, page, user.getId());
        return ResponseEntity.ok(response);
    }

    // 팔로잉 목록 조회
    @GetMapping("/{uid}/followings")
    public ResponseEntity<FollowingListResponse> followingList(@CurrentUser User user,
                                                               @PathParam("uid") Long id,
                                                               @RequestParam(value = "p", defaultValue = "1") int page) {
        FollowingListResponse response = followService.pagedFollowings(id, false, page, user.getId());
        return ResponseEntity.ok(response);
    }
}
