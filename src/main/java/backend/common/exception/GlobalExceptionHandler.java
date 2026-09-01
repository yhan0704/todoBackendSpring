package backend.common.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("EMAIL_ALREADY_EXISTS", e.getMessage()));
    }

    // DB unique 제약을 우회할 만큼 빠른 동시 요청이 겹쳤을 때의 최종 방어선
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("EMAIL_ALREADY_EXISTS", "이미 가입된 이메일입니다."));
    }

    // 로그인 실패(이메일 불일치/비밀번호 불일치)를 401로 응답 — 이 핸들러가 없으면 AuthService가 던지는
    // RuntimeException이 그대로 스프링 기본 에러 처리로 넘어가 500으로 응답되어 클라이언트가
    // "인증 실패"와 "서버 오류"를 구분할 수 없었음
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("INVALID_CREDENTIALS", e.getMessage()));
    }

    // refresh 토큰이 만료/위조/폐기(로그아웃, 회전)된 경우를 401로 응답 — 위와 동일한 이유로
    // 커스텀 예외 + 전용 핸들러가 필요했음
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("INVALID_TOKEN", e.getMessage()));
    }

    // @Valid 검증 실패를 400 + 필드별 에러 메시지로 응답 — 검증 애노테이션만 붙이고 이 핸들러가
    // 없으면 스프링이 자체 포맷으로 400을 내려줘 프론트가 필드별 에러를 파싱하기 어려움
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationFailed(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_REQUEST", message));
    }
}
