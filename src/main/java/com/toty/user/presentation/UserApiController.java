package com.toty.user.presentation;

import com.toty.annotation.CurrentUser;
import com.toty.following.application.FollowService;
import com.toty.following.presentation.dto.response.FollowingListResponse;
import com.toty.user.application.UserService;
import com.toty.user.domain.User;
import com.toty.user.presentation.dto.request.UserInfoUpdateRequest;
import com.toty.user.presentation.dto.request.UserSignUpRequest;
import com.toty.user.presentation.dto.response.UserInfoResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.server.PathParam;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

//
@Controller
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;
    private final FollowService followService;

    // 회원가입
    @PostMapping("/")
    @ResponseBody
    public ResponseEntity<Long> signUp(@RequestBody UserSignUpRequest userSignUpRequest) {

        Long userId = userService.signUp(userSignUpRequest);
        return new ResponseEntity<>(userId, HttpStatus.CREATED);
    }

    // 정보 수정(api)
    @PatchMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType
            .MULTIPART_FORM_DATA_VALUE})
    @ResponseBody
        public ResponseEntity<String> updateUserInfo(@CurrentUser User user, @RequestPart UserInfoUpdateRequest newInfo, @RequestPart
        MultipartFile imgFile){
        userService.updateUser(user.getId(), newInfo, imgFile);
        return ResponseEntity.ok("Done");
    }

    // 정보 보기
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<UserInfoResponse> getUserInfo(@CurrentUser User user, @PathVariable("id") Long id, HttpSession session) {
        // 본인인지 아닌지 확인 -> 아니면 약식 정보만 전달
        UserInfoResponse userInfo = userService.getUserInfoResponse(user, id);
        return ResponseEntity.ok(userInfo);
    }

    // 이메일 중복확인
    @GetMapping("/check-email") // 파라미터로 받아오기
    @ResponseBody
    public ResponseEntity emailValidation(@RequestParam(name = "email") String email) {
        String response = userService.validateEmail(email);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 닉네임
    @GetMapping("/check-nickname")
    @ResponseBody
    public ResponseEntity nicknameValidation(@RequestParam(name = "nickname") String nickname) {
        String response = userService.validateNickname(nickname);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("{uid}/posts")
    @ResponseBody
    public ResponseEntity getPostByCategory(@PathVariable("uid") Long id) {
        // todo
        Map<String, String> response = new HashMap<>();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 팔로워 목록 조회
    @GetMapping("/{uid}/followers")
    @ResponseBody
    public ResponseEntity<FollowingListResponse> followersList(@PathParam("uid") Long id, @RequestParam(value = "p", defaultValue = "1") int page) {
        FollowingListResponse response = followService.pagedFollowings(id, true, page);
        return ResponseEntity.ok(response);
    }

    // 팔로잉 목록 조회
    @GetMapping("/{uid}/followings")
    @ResponseBody
    public ResponseEntity<FollowingListResponse> followingList(@PathParam("uid") Long id, @RequestParam(value = "p", defaultValue = "1") int page) {
        FollowingListResponse response = followService.pagedFollowings(id, false, page);
        return ResponseEntity.ok(response);
    }


}