package com.toty.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSignUpRequest {
    @Email(message = "유효한 이메일 주소를 입력하세요.")
    @NotBlank(message = "이메일을 입력하세요.")
    private String email;

    @NotBlank(message = "비밀번호를 입력하세요.")
    @Size(min = 8, max = 32, message = "비밀번호는 8자 이상 32자 이하여야 합니다.")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
        message = "비밀번호는 대소문자, 숫자, 특수문자를 포함해야 합니다."
    )
    private String password;

    @NotBlank(message = "사용자명을 입력하세요.")
    @Size(min = 2, max = 50, message = "사용자명은 2자 이상 50자 이하여야 합니다.")
    private String username;

    @NotBlank(message = "닉네임을 입력하세요.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
    @Pattern(
        regexp = "^[가-힣a-zA-Z0-9_-]+$",
        message = "닉네임은 한글, 알파벳, 숫자, 언더스코어, 하이픈만 포함 가능합니다."
    )
    private String nickname;

    @NotBlank(message = "휴대폰 번호를 입력하세요.")
    @Pattern(
        regexp = "^01[0-9]-\\d{3,4}-\\d{4}$",
        message = "휴대폰 번호는 010-0000-0000 형식이어야 합니다."
    )
    private String phoneNumber;
}