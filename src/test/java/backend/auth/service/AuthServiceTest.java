package backend.auth.service;

import backend.auth.dto.request.SignupRequest;
import backend.common.exception.EmailAlreadyExistsException;
import backend.user.entity.User;
import backend.user.repository.UserRepository;
import backend.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("이메일이 중복되지 않으면 회원가입에 성공하고, 이메일은 소문자/trim으로 정규화되어 저장된다")
    void signup_success_whenEmailNotDuplicate() {
        SignupRequest request = new SignupRequest("  New@Example.com  ", "password1234", "홍길동");
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");

        authService.signup(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("new@example.com");
    }

    @Test
    @DisplayName("이미 가입된 이메일이면 EmailAlreadyExistsException을 던지고 저장을 시도하지 않는다")
    void signup_throws_whenEmailDuplicate() {
        SignupRequest request = new SignupRequest("existing@example.com", "password1234", "홍길동");
        User existingUser = User.builder().email(request.email()).build();
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("이미 가입된 이메일입니다.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("대소문자/공백만 다른 이메일도 정규화 후 비교되어 중복으로 처리된다")
    void signup_throws_whenEmailDuplicateWithDifferentCaseAndWhitespace() {
        SignupRequest request = new SignupRequest("  Existing@Example.com  ", "password1234", "홍길동");
        User existingUser = User.builder().email("existing@example.com").build();
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any(User.class));
    }
}
