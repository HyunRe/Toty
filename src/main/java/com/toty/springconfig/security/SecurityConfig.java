package com.toty.springconfig.security;

import com.toty.springconfig.security.jwt.CustomAuthenticationEntryPoint;
import com.toty.springconfig.security.jwt.JwtRequestFilter;
import com.toty.springconfig.security.jwt.AccessTokenValidationFilter;
import com.toty.springconfig.security.oauth2.MyOAuth2UserService;
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
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 추가 예정
//    @Autowired
//    private AuthenticationFailureHandler failureHandler;
//    @Autowired
    private final MyOAuth2UserService myOAuth2UserService;

    private final SavedRequestAwareAuthenticationSuccessHandler formloginsuccess;
    private final JwtRequestFilter jwtRequestFilter;
    private final AccessTokenValidationFilter accessTokenValidationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(auth -> auth.disable())       // CSRF 방어 기능 비활성화
                .headers(x -> x.frameOptions(y -> y.disable()))
                .authorizeHttpRequests(requests -> requests
                        // 테스트 엔드포인트
                        .requestMatchers("/api/users/test").hasAuthority("USER")
                        //멘토만 접근 가능한 url
//                        .requestMatchers(HttpMethod.POST, "").hasAuthority("MENTOR")
                        //관리자만 접근 가능한 url
//                        .requestMatchers("").hasAuthority("ADMIN")
                        .anyRequest().permitAll()
                )
                .formLogin(auth -> auth
                        .loginProcessingUrl("/api/users/sign-in")  // post 엔드포인트
                        .usernameParameter("email")
                        .passwordParameter("pwd")
                        .successHandler(formloginsuccess)
                        .failureHandler(loginFailureHandler())
                        .permitAll()
                )
                .logout(auth -> auth
                        .logoutUrl("/api/users/sign-out")
                        .deleteCookies("JSESSIONID")
                        .deleteCookies("accessToken")
                        .deleteCookies("refreshToken")
                        .logoutSuccessUrl("/view/users/home")
                )
                // 추가 예정
                .oauth2Login(auth -> auth
                        .userInfoEndpoint(user -> user.userService(myOAuth2UserService))
                        .successHandler(formloginsuccess)
                        .failureHandler(loginFailureHandler())
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 토큰 관련 Filter 추가
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class); // /api/users/sign-in, /api/auth/refresh 제외 모든 경로
        http.addFilterAfter(accessTokenValidationFilter, ExceptionTranslationFilter.class); // /api/auth/refresh 경로만 -> ok면

        return http.build();
    }

    @Bean
    public SimpleUrlAuthenticationFailureHandler loginFailureHandler() {
        SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();
        failureHandler.setDefaultFailureUrl("/api/users/sign-out"); // 인증 실패 시
        return failureHandler;
    }

}
