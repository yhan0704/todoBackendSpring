package backend.auth.controller;

import backend.auth.dto.request.LoginRequest;
import backend.auth.dto.request.RefreshTokenRequest;
import backend.auth.dto.request.SignupRequest;
import backend.auth.dto.response.LoginResponse;
import backend.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // @Valid로 SignupRequest의 Bean Validation 애노테이션을 실제로 동작시킴 —
    // 이게 없으면 DTO에 @NotBlank/@Email을 붙여도 검증이 수행되지 않고 그대로 서비스까지 통과함
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }
}