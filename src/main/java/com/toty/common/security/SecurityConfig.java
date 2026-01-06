package com.toty.common.security;

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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // [HTTPS 강제 설정]
                // Nginx가 헤더에 담아 보낸 X-Forwarded-Proto: https를 확인합니다.
                // 만약 사용자가 http://로 접속하면 자동으로 https://로 리다이렉트 시킵니다.
                .requiresChannel(channel -> channel.anyRequest().requiresSecure())

                .csrf(auth -> auth.disable())       // REST API이므로 CSRF 보안은 비활성화
                .headers(x -> x.frameOptions(y -> y.disable())) // H2 콘솔 등을 사용할 때 프레임 차단 해제

                .authorizeHttpRequests(requests -> requests
                        // [정적 리소스] 인증 없이 접근 가능하도록 허용
                        .requestMatchers("/posts/images/**", "/css/**", "/js/**", "/img/**", "/static/**", "/favicon.ico").permitAll()
                        .requestMatchers("/firebase-messaging-sw.js").permitAll()

                        // [Swagger/API 문서] 개발용 문서 접근 허용
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                        // [로그인/회원가입] 누구나 접근 가능해야 하는 경로
                        .requestMatchers("/view/users/login", "/view/users/sign-in", "/view/users/signup").permitAll()
                        .requestMatchers("/api/users/sign-in", "/api/users/sign-up", "/api/users/signup").permitAll()
                        .requestMatchers("/api/users/check-email", "/api/users/check-nickname", "/api/users/authCode", "/api/users/check-authCode").permitAll()

                        // [OAuth2/에러/액츄에이터]
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/error", "/actuator/**").permitAll()

                        // [인증 필요] 로그인한 사용자만 접근 가능한 경로
                        .requestMatchers("/api/sse/**", "/api/notifications/**", "/api/images/**").authenticated()

                        // [나머지] 그 외 모든 요청은 로그인이 필요함
                        .anyRequest().authenticated()
                )
                .formLogin(auth -> auth
                        .loginProcessingUrl("/api/users/sign-in")
                        .usernameParameter("email")
                        .passwordParameter("pwd")
                        .successHandler(successfulnesses)
                        .failureHandler(loginFailureHandler())
                        .permitAll()
                )
                // ... (기존 logout 및 oauth2Login 설정 유지)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // JWT 사용 시 세션을 생성하지 않음

        // JWT 필터 순서 설정
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
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
