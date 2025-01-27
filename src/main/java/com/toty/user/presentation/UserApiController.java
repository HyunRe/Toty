package com.toty.user.presentation;

import com.toty.user.application.UserService;
import com.toty.user.domain.UserRepository;
import com.toty.user.presentation.dto.request.UserInfoUpdateRequest;
import com.toty.user.presentation.dto.request.UserSignUpRequest;
import com.toty.user.presentation.dto.response.UserInfoResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.server.PathParam;
import java.io.File;
import java.io.IOException;
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

// 컨트롤러 요소 빠진 것 없는지,
//
@Controller
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;

    @GetMapping("/signup")
    public String signUpProc(){
        return "signup";
    }

    // 회원 가입
    @PostMapping("/")
    @ResponseBody
    public ResponseEntity<Long> signUp(@RequestBody UserSignUpRequest userSignUpRequest) {

        Long userId = userService.signUp(userSignUpRequest);
        return new ResponseEntity<>(userId, HttpStatus.CREATED);
    }

    // 정보 수정(View)
    @GetMapping("/edit-form/{id}")
    public String updateProc(@PathVariable Long id, Model model){
        // 본인인지 확인 -> 아니면 예외

        // 데이터 DTO에 담기(True)
        UserInfoResponse userInfo = userService.getUserInfo(id,true); // email은 readonly로..
        model.addAttribute("userInfo", userInfo);
        return "update";
    }

    // 정보 수정(api)
    @PatchMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType
            .MULTIPART_FORM_DATA_VALUE})
    @ResponseBody
        public ResponseEntity<String> postFile(@PathParam("id") Long id, @RequestPart UserInfoUpdateRequest newInfo, @RequestPart
        MultipartFile imgFile){ // 하나씩 다 쪼개는거..? 태그, 이미지, site -> delete 요청
        userService.updateUser(id, newInfo, imgFile);
        return ResponseEntity.ok("Done");
    }

    // 정보 보기
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<UserInfoResponse> getUserInfo(@PathVariable("id") Long id, HttpSession session) {
        // 본인인지 아닌지 확인 -> 아니면 약식 정보만 전달
        Long uid = (Long) session.getAttribute("uid");
        if (id == uid) {
            UserInfoResponse userInfo = userService.getUserInfo(id, false);
        }

        // todo getUserInfo 본인인지 아닌지 여부 필드 추가
        UserInfoResponse userInfo = userService.getUserInfo(id,true);
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

    // 회원 탈퇴(view)
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        // 본인 확인 로직 -> 아니면 예외
        userService.deleteUser(id); //soft delete
        return "redirect:/api/users/custom-login";
    }

    @GetMapping("{uid}/posts")
    @ResponseBody
    public ResponseEntity getPostByCategory(@PathVariable Long id) {
        // todo
        Map<String, String> response = new HashMap<>();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}