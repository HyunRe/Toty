package com.toty.common.security;

import com.toty.common.security.jwt.CustomAuthenticationEntryPoint;
import com.toty.common.security.jwt.JwtRequestFilter;
import com.toty.common.security.jwt.AccessTokenValidationFilter;
import com.toty.common.security.oauth2.MyOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final MyOAuth2UserService myOAuth2UserService;
    private final SavedRequestAwareAuthenticationSuccessHandler successfulnesses;
    private final JwtRequestFilter jwtRequestFilter;
    private final AccessTokenValidationFilter accessTokenValidationFilter;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(auth -> auth.disable())       // CSRF 방어 기능 비활성화
                .headers(x -> x.frameOptions(y -> y.disable()))
                .authorizeHttpRequests(requests -> requests
                        // 정적 리소스 - 인증 불필요
                        .requestMatchers("/posts/images/**", "/css/**", "/js/**", "/img/**", "/static/**", "/favicon.ico").permitAll()
                        .requestMatchers("/firebase-messaging-sw.js").permitAll()  // Firebase Service Worker

                        // Swagger UI - 인증 불필요
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                        // Firebase Service Worker
                        .requestMatchers("/firebase-messaging-sw.js").permitAll()

                        // 로그인/회원가입 관련 - 인증 불필요
                        .requestMatchers("/view/users/login", "/view/users/sign-in").permitAll()
                        .requestMatchers("/view/users/signup").permitAll() // GET, POST 모두 허용
                        .requestMatchers("/api/users/sign-in", "/api/users/sign-up", "/api/users/signup").permitAll()
                        .requestMatchers("/api/users/check-email", "/api/users/check-nickname").permitAll() // 중복 확인
                        .requestMatchers("/api/users/authCode", "/api/users/check-authCode").permitAll() // SMS 인증
                        .requestMatchers("/view/users/alert/**").permitAll()

                        // 이메일/비밀번호 찾기 - 인증 불필요
                        .requestMatchers("/view/users/find-email", "/view/users/reset-password").permitAll()
                        .requestMatchers("/api/users/find-email", "/api/users/reset-password").permitAll()

                        // OAuth2 관련 경로
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                        // 에러 페이지 - 인증 불필요
                        .requestMatchers("/error").permitAll()

                        // Actuator endpoint
                        .requestMatchers("/actuator/**").permitAll()

                        // SSE 구독 (로그인 필수)
                        .requestMatchers("/api/sse/**").authenticated()

                        // FCM 토큰 등록 / 삭제
                        .requestMatchers("/api/notifications/fcm/**").authenticated()

                        // 알림 조회 / 읽음 / 삭제
                        .requestMatchers("/api/notifications/**").authenticated()

                        // 이미지 업로드 (로그인 필수)
                        .requestMatchers("/api/images/**").authenticated()

                        // 나머지 모든 요청은 인증 필요 (로그인 필수!)
                        .anyRequest().authenticated()
                )
                .formLogin(auth -> auth
                        .loginProcessingUrl("/api/users/sign-in")  // post 엔드포인트
                        .usernameParameter("email")
                        .passwordParameter("pwd")
                        .successHandler(successfulnesses)
                        .failureHandler(loginFailureHandler())
                        .permitAll()
                )
                .logout(auth -> auth
                        .logoutUrl("/api/users/sign-out")
                        .deleteCookies("JSESSIONID")
                        .deleteCookies("accessToken")
                        .deleteCookies("refreshToken")
                        .logoutSuccessUrl("/view/users/alert/logout")
                )
                .oauth2Login(auth -> auth
                        .userInfoEndpoint(user -> user.userService(myOAuth2UserService))
                        .successHandler(successfulnesses)
                        .failureHandler(loginFailureHandler())
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint))
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(customAccessDeniedHandler))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 토큰 관련 Filter 추가
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class); // 인증 경로 제외 모든 경로
        http.addFilterAfter(accessTokenValidationFilter, ExceptionTranslationFilter.class);

        return http.build();
    }

    @Bean
    public SimpleUrlAuthenticationFailureHandler loginFailureHandler() {
        SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();
        failureHandler.setDefaultFailureUrl("/view/users/alert/login-fail");
        return failureHandler;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/css/**", "/js/**", "/img/**", "/images/**", "/static/**");
    }
}
