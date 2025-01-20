package service;

import com.toty.user.application.UserService;
import com.toty.user.domain.User;
import com.toty.user.domain.UserRepository;
import com.toty.user.presentation.dto.request.UserSignUpRequest;
import com.toty.user.presentation.dto.response.UserInfoResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원 가입 성공")
    void userSignUpSuccess() {
        // given
        UserSignUpRequest userSignUpRequest = new UserSignUpRequest("test@gmail.com", "test123");
        User newUser = new User(userSignUpRequest.getEmail(), userSignUpRequest.getPassword());
        ReflectionTestUtils.setField(newUser, "id", 3L);

        BDDMockito.given(userRepository.findByEmail(userSignUpRequest.getEmail()))
                .willReturn(Optional.empty());
        BDDMockito.given(userRepository.save(ArgumentMatchers.any(User.class)))
                .willReturn(newUser);

        // when
        Long userId = userService.signUp(userSignUpRequest);

        // then
        Assertions.assertThat(userId).isEqualTo(3L);
        BDDMockito.then(userRepository).should().save(ArgumentMatchers.any(User.class));
    }

    @Test
    @DisplayName("내 정보 찾기 성공")
    void getUserInfoSuccess() {
        // given
        User existingUser = new User("test@gmail.com", "test123");
        ReflectionTestUtils.setField(existingUser, "id", 3L);

        BDDMockito.given(userRepository.findById(existingUser.getId())).willReturn(Optional.of(existingUser));

        // when
        UserInfoResponse result = userService.getUserInfo(existingUser.getId());

        // then
        Assertions.assertThat(result.getEmail()).isEqualTo(existingUser.getEmail());
    }
}