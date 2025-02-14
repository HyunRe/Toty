package com.toty.springconfig.security.oauth2;

import com.toty.user.application.UserService;
import com.toty.user.domain.model.LoginProvider;
import com.toty.user.domain.model.User;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Service
@RequiredArgsConstructor
public class MyOAuth2UserService extends DefaultOAuth2UserService {
    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String email, uname, profileUrl;
        String hashedPwd = bCryptPasswordEncoder.encode("Social Login");
        User user = null;

        String accessToken = userRequest.getAccessToken().getTokenValue();
        System.out.println("액세스 토큰 발급완료 : "+ !accessToken.isBlank());

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        switch (provider) {

            case "kakao":
                Map<String, Object> account = (Map) oAuth2User.getAttribute("kakao_account");
                email = (String) account.get("email");
                try {
                    user = userService.findByEmail(email);
                } catch (Exception e) {
                    long kid = (long) oAuth2User.getAttribute("id");
                    Map<String, String> properties = (Map) oAuth2User.getAttribute(
                            "properties");
                    account = (Map) oAuth2User.getAttribute("kakao_account");
                    String nickname = (String) properties.get("nickname");
                    nickname = (nickname == null) ? "k_" + kid : nickname;
                    email = (String) account.get("email");
                    profileUrl = (String) properties.get("profile_image");
                    user = User.builder()
                            .email(email)
                            .password(hashedPwd)
                            .username(nickname)
                            .nickname(nickname)
                            .smsSubscribed(false)
                            .emailSubscribed(false)
                            .notificationAllowed(false)
                            .loginProvider(LoginProvider.KAKAO)
                            .build();
                    userService.registerUser(user);
                }
                break;

            case "google":
                email = oAuth2User.getAttribute("email");
                try {
                    user = userService.findByEmail(email);
                } catch (Exception e) {
                        uname = oAuth2User.getAttribute("name");
                        String sub = oAuth2User.getAttribute("sub");
                        uname = (uname == null) ? "g_"+sub : uname;
                        email = oAuth2User.getAttribute("email");
                        profileUrl = oAuth2User.getAttribute("picture");
                        user = User.builder()
                                .email(email)
                                .password(hashedPwd)
                                .username(uname)
                                .nickname(uname)
                                .smsSubscribed(false)
                                .emailSubscribed(false)
                                .notificationAllowed(false)
                                .loginProvider(LoginProvider.GOOGLE)
                                .build();
                        userService.registerUser(user);
                }
                break;

            case "github": // 깃허브 이메일 필수값 아님 -> 자체 email로 대체
                int id = oAuth2User.getAttribute("id");
                email = oAuth2User.getAttribute("email");
                email = (email == null) ? id+"@myfeed.com": email;
                try {
                    user = userService.findByEmail(email);
                } catch(Exception e) {
                    uname = oAuth2User.getAttribute("login");
                    uname = (uname == null) ? "g_" + id : uname;
                    profileUrl = oAuth2User.getAttribute("avatar_url");
                    user = User.builder()
                            .email(email)
                            .password(hashedPwd)
                            .username(uname)
                            .nickname(uname)
                            .smsSubscribed(false)
                            .emailSubscribed(false)
                            .notificationAllowed(false)
                            .loginProvider(LoginProvider.GITHUB)
                            .build();
                    userService.registerUser(user);
                }
                break;

        }
        return new MyUserDetails(user, oAuth2User.getAttributes());
    }
}

