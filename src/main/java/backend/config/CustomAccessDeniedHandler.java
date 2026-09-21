package backend.config;

import backend.common.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 인증은 됐지만(=유효한 토큰) 권한이 없어서 거부되는 진짜 403 케이스를 위한 핸들러.
// 지금은 역할(Role) 기반 인가가 없어서 실제로 발생하진 않지만, CustomAuthenticationEntryPoint와
// 같은 응답 포맷을 유지하기 위해 함께 등록해둠
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = new ErrorResponse("ACCESS_DENIED", "접근 권한이 없습니다.");
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
