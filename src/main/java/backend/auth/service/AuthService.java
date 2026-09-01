package backend.auth.service;
import lombok.extern.slf4j.Slf4j;
import backend.common.exception.EmailAlreadyExistsException;
import backend.common.exception.InvalidCredentialsException;
import backend.common.exception.InvalidTokenException;
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

    // 이메일 중복 체크 후 저장. 성공 시 회원가입 감사 로그를 남겨 이후 로그인 실패 로그와
    // 대조(가입은 했는데 로그인을 계속 실패하는 패턴 등)할 수 있게 함
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
        log.info("signup success: email={}", email);
    }

    // 이메일 미존재/비밀번호 불일치를 구분하지 않고 동일한 InvalidCredentialsException으로 통일
    // (계정 존재 여부를 노출하는 이메일 이넘어레이션(enumeration) 공격 방지). 실패 시 브루트포스
    // 탐지용으로 로그만 남기고, 어떤 사유인지는 응답에 드러내지 않음
    public LoginResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("login failed - no such user: email={}", email);
                    return new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
                });
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("login failed - password mismatch: email={}", email);
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }

        return issueTokens(user);
    }

    // refresh token은 DB에 해시로만 저장되므로(issueTokens 참고) 여기서도 평문 토큰을 그대로
    // equals 비교할 수 없어 passwordEncoder.matches로 검증. 서명/만료/보유 여부 중 하나라도
    // 실패하면 모두 같은 InvalidTokenException으로 처리해 실패 사유를 응답에 노출하지 않음
    public LoginResponse refresh(RefreshTokenRequest request) {
        String token = request.refreshToken();

        if (!jwtUtil.validateRefreshToken(token)) {
            log.warn("refresh failed - invalid or expired token");
            throw new InvalidTokenException(INVALID_REFRESH_TOKEN_MESSAGE);
        }

        String email = jwtUtil.getEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("refresh failed - no such user: email={}", email);
                    return new InvalidTokenException(INVALID_REFRESH_TOKEN_MESSAGE);
                });

        // 저장된 해시와 매칭되지 않으면 이미 로그아웃했거나, 회전(rotate)되어 폐기된 토큰
        if (user.getRefreshToken() == null || !passwordEncoder.matches(token, user.getRefreshToken())) {
            log.warn("refresh failed - token not matched or revoked: email={}", email);
            throw new InvalidTokenException(INVALID_REFRESH_TOKEN_MESSAGE);
        }

        return issueTokens(user);
    }

    // 로그아웃은 실패해도 사용자에게 에러를 보여줄 필요가 없는 멱등 동작이라 예외를 던지지 않고
    // 조용히 무시. 저장된 해시와 매칭되는 경우에만 refresh token을 폐기(null)해서, 이미 만료/폐기된
    // 토큰으로 다른 세션의 토큰을 지우는 일이 없게 함
    public void logout(RefreshTokenRequest request) {
        String token = request.refreshToken();

        if (!jwtUtil.validateRefreshToken(token)) {
            return;
        }

        userRepository.findByEmail(jwtUtil.getEmail(token))
                .filter(user -> user.getRefreshToken() != null && passwordEncoder.matches(token, user.getRefreshToken()))
                .ifPresent(user -> {
                    user.updateRefreshToken(null);
                    userRepository.save(user);
                });
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    // refresh token을 평문이 아닌 BCrypt 해시로 저장 — DB가 유출되어도 저장된 값만으로는
    // 토큰을 복원할 수 없어 바로 계정 탈취로 이어지지 않음. 비교는 refresh()/logout()에서
    // passwordEncoder.matches로 수행
    private LoginResponse issueTokens(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        // refresh token을 회전(rotate)시켜 저장 — 탈취된 이전 토큰은 더 이상 쓸 수 없음
        user.updateRefreshToken(passwordEncoder.encode(refreshToken));
        userRepository.save(user);

        return new LoginResponse(accessToken, refreshToken);
    }
}
