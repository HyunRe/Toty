package com.toty.springconfig.security;

import com.toty.springconfig.security.jwt.CustomAuthenticationEntryPoint;
import com.toty.springconfig.security.jwt.JwtRequestFilter;
import com.toty.springconfig.security.jwt.AccessTokenValidationFilter;
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

    private final SavedRequestAwareAuthenticationSuccessHandler formloginsuccess;
    private final JwtRequestFilter jwtRequestFilter;
    private final AccessTokenValidationFilter accessTokenValidationFilter;
    private final CookieRequestCache cookieRequestCache;

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
                        .loginPage("/common/home") // template 이하 경로
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

    @Bean
    public SimpleUrlAuthenticationFailureHandler loginFailureHandler() {
        SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();
        failureHandler.setDefaultFailureUrl("/login");
        failureHandler.setAllowSessionCreation(false);
        return failureHandler;
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
