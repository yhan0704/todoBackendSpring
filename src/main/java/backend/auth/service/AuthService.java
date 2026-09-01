package backend.auth.service;
import lombok.extern.slf4j.Slf4j;
import backend.common.exception.EmailAlreadyExistsException;
import backend.auth.dto.request.LoginRequest;
import backend.auth.dto.request.RefreshTokenRequest;
import backend.auth.dto.request.SignupRequest;
import backend.auth.dto.response.LoginResponse;
import backend.user.entity.User;
import backend.user.repository.UserRepository;
import backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "이메일 또는 비밀번호가 올바르지 않습니다.";
    private static final String INVALID_REFRESH_TOKEN_MESSAGE = "유효하지 않은 토큰입니다.";
    private static final String EMAIL_ALREADY_EXISTS_MESSAGE = "이미 가입된 이메일입니다.";

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public void signup(SignupRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException(EMAIL_ALREADY_EXISTS_MESSAGE);
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.builder()
                .email(email)
                .password(encodedPassword)
                .name(request.name())
                .build();

        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(() -> new RuntimeException(INVALID_CREDENTIALS_MESSAGE));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException(INVALID_CREDENTIALS_MESSAGE);
        }

        return issueTokens(user);
    }

    public LoginResponse refresh(RefreshTokenRequest request) {
        String token = request.refreshToken();

        if (!jwtUtil.validateRefreshToken(token)) {
            throw new RuntimeException(INVALID_REFRESH_TOKEN_MESSAGE);
        }

        String email = jwtUtil.getEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(INVALID_REFRESH_TOKEN_MESSAGE));

        // 저장된 refresh token과 다르면 이미 로그아웃했거나, 회전(rotate)되어 폐기된 토큰
        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(token)) {
            throw new RuntimeException(INVALID_REFRESH_TOKEN_MESSAGE);
        }

        return issueTokens(user);
    }

    public void logout(RefreshTokenRequest request) {
        String token = request.refreshToken();

        if (!jwtUtil.validateRefreshToken(token)) {
            return;
        }

        userRepository.findByEmail(jwtUtil.getEmail(token))
                .filter(user -> token.equals(user.getRefreshToken()))
                .ifPresent(user -> {
                    user.updateRefreshToken(null);
                    userRepository.save(user);
                });
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private LoginResponse issueTokens(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        // refresh token을 회전(rotate)시켜 저장 — 탈취된 이전 토큰은 더 이상 쓸 수 없음
        user.updateRefreshToken(refreshToken);
        userRepository.save(user);

        return new LoginResponse(accessToken, refreshToken);
    }
}
