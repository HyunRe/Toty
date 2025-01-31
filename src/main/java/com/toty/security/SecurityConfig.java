package com.toty.security;

import com.toty.jwt.CustomAuthenticationEntryPoint;
import com.toty.jwt.JwtRequestFilter;
import com.toty.jwt.RefreshTokenValidationFilter;
import com.toty.user.domain.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.CookieRequestCache;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {


    // 추가 예정
//    @Autowired
//    private AuthenticationFailureHandler failureHandler;
//    @Autowired
//    private MyOAuth2UserService myOAuth2UserService;

    private final AuthenticationSuccessHandler authSuccessHandler;
    private final JwtRequestFilter jwtRequestFilter;
    private final RefreshTokenValidationFilter refreshTokenValidationFilter;
    private final CookieRequestCache cookieRequestCache;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(auth -> auth.disable())       // CSRF 방어 기능 비활성화
                .headers(x -> x.frameOptions(y -> y.disable()))
                .authorizeHttpRequests(requests -> requests
//                        .requestMatchers(HttpMethod.POST, "").hasRole(String.valueOf(Role.USER))
//                        .requestMatchers("").hasRole(String.valueOf(Role.USER))
//                        .requestMatchers(HttpMethod.POST, "").hasRole(String.valueOf(Role.MENTOR))
//                        .requestMatchers("").hasRole(String.valueOf(Role.MENTOR))
//                        .requestMatchers("").hasRole(String.valueOf(Role.ADMIN))
                        .anyRequest().permitAll()
                )
                .formLogin(auth -> auth
                        .loginPage("/home") // template return url
                        .loginProcessingUrl("/api/users/sign-in")  // post 엔드포인트
                        .usernameParameter("email")
                        .passwordParameter("pwd")
                        //.defaultSuccessUrl("/api/home", true)
                        .successHandler(authSuccessHandler)
//                        .failureHandler(failureHandler)
                        .permitAll()
                )
                .logout(auth -> auth
                        .logoutUrl("/api/users/sign-out")
                        .deleteCookies("JSESSIONID")
                        .deleteCookies("accessToken")
                        .deleteCookies("refreshToken")
                        .logoutSuccessUrl("/home")
                )
                // 추가 예정
//                .oauth2Login(auth -> auth
//                        .userInfoEndpoint(user -> user.userService(myOAuth2UserService))
//                        .successHandler(authSuccessHandler)
//                        .failureHandler(failureHandler)
//                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ) // 세션 비활성화
                .requestCache(requestCache -> requestCache
                        .requestCache(cookieRequestCache));

        // 토큰 관련 Filter 추가
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class); // /api/users/sign-in, /api/auth/refresh 제외 모든 경로
        http.addFilterBefore(refreshTokenValidationFilter, JwtRequestFilter.class); // /api/auth/refresh 경로만

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
