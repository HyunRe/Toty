package com.toty.common.security.authentication;

import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("========== MyUserDetailsService.loadUserByUsername ==========");
        log.info("로그인 시도 이메일: {}", email);

        User user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.error("사용자를 찾을 수 없음: {}", email);
            return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email);
        });

        log.info("사용자 찾음 - ID: {}, 이메일: {}", user.getId(), user.getEmail());
        log.info("DB 비밀번호 (처음 20자): {}", user.getPassword().substring(0, Math.min(20, user.getPassword().length())));
        log.info("비밀번호 길이: {}", user.getPassword().length());
        log.info("비밀번호가 BCrypt 형식인가? {}", user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2b$"));

        return new AccountAdapter(user);
    }

}
