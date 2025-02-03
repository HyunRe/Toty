package com.toty.security;

import com.toty.jwt.CustomAuthenticationEntryPoint;
import com.toty.jwt.JwtRequestFilter;
import com.toty.jwt.JwtTokenUtil;
import com.toty.jwt.AccessTokenValidationFilter;
import com.toty.user.domain.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
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
    private final AccessTokenValidationFilter accessTokenValidationFilter;
    private final JwtTokenUtil jwtTokenUtil;
    private final CookieRequestCache cookieRequestCache;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(auth -> auth.disable())       // CSRF 방어 기능 비활성화
                .headers(x -> x.frameOptions(y -> y.disable()))
                .authorizeHttpRequests(requests -> requests
//                        .requestMatchers(HttpMethod.POST, "").hasRole(String.valueOf(Role.USER))
                        .requestMatchers("/api/users/test").hasRole(String.valueOf(Role.USER))
//                        .requestMatchers(HttpMethod.POST, "").hasRole(String.valueOf(Role.MENTOR))
//                        .requestMatchers("").hasRole(String.valueOf(Role.MENTOR))
//                        .requestMatchers("").hasRole(String.valueOf(Role.ADMIN))
                        .anyRequest().permitAll()
                )
                .formLogin(auth -> auth
//                        .loginPage("/home") // template return url
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
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(requestCache -> requestCache
                        .requestCache(cookieRequestCache));

        // 토큰 관련 Filter 추가
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class); // /api/users/sign-in, /api/auth/refresh 제외 모든 경로
//        http.addFilterBefore(refreshTokenAuthenticationFilter(am), UsernamePasswordAuthenticationFilter.class); // /api/auth/refresh 만
        http.addFilterAfter(accessTokenValidationFilter, ExceptionTranslationFilter.class); // /api/auth/refresh 경로만 -> ok면

        return http.build();
    }

    // 방화벽
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.httpFirewall(defaultHttpFirewall());
    }

    @Bean
    public HttpFirewall defaultHttpFirewall() {
        return new DefaultHttpFirewall();
    }

}
