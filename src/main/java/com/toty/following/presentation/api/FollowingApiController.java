package com.toty.following.presentation.api;

import com.toty.common.annotation.CurrentUser;
import com.toty.following.application.service.FollowingService;
import com.toty.following.dto.request.FollowingRequest;
import com.toty.following.dto.response.FollowingListResponse;
import com.toty.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Follow", description = "팔로우 API")
@RequestMapping("/api/follow")
@RestController
@RequiredArgsConstructor
public class FollowingApiController {
    private final FollowingService followingService;

    @Operation(summary = "팔로우", description = "특정 사용자를 팔로우합니다")
    @PostMapping("/")
    public ResponseEntity<Long> follow(@CurrentUser User user,
                                       @RequestBody FollowingRequest followingRequest) {
        Long response = followingService.follow(user.getId(), followingRequest.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "언팔로우", description = "특정 사용자를 언팔로우합니다")
    @DeleteMapping("/{id}")
    public ResponseEntity<Long> unfollow(@CurrentUser User user, @PathVariable("id") Long id) {
        Long response = followingService.unfollow(user.getId(), id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "팔로워 목록 조회", description = "특정 사용자를 팔로우하는 사용자 목록을 페이지네이션으로 조회합니다")
    @GetMapping("/{uid}/followers")
    public ResponseEntity<FollowingListResponse> followersList(@CurrentUser User user,
                                                               @PathVariable("uid") Long id,
                                                               @RequestParam(value = "p", defaultValue = "1") int page) {
        FollowingListResponse response = followingService.pagedFollowings(id, true, page, user.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "팔로잉 목록 조회", description = "특정 사용자가 팔로우하는 사용자 목록을 페이지네이션으로 조회합니다")
    @GetMapping("/{uid}/followings")
    public ResponseEntity<FollowingListResponse> followingList(@CurrentUser User user,
                                                               @PathVariable("uid") Long id,
                                                               @RequestParam(value = "p", defaultValue = "1") int page) {
        FollowingListResponse response = followingService.pagedFollowings(id, false, page, user.getId());
        return ResponseEntity.ok(response);
    }
}
