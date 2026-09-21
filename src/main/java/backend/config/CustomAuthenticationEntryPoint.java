package backend.config;

import backend.common.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// JwtAuthenticationFilter가 토큰을 못 채워서 SecurityContext가 비어있는 채로 authorizeHttpRequests의
// authenticated() 검사를 통과하지 못하면 Spring Security가 이 클래스를 호출함. 이게 없으면
// 기본값인 Http403ForbiddenEntryPoint가 대신 동작해서, 토큰이 없거나 만료됐을 뿐인데도(=401이어야 함)
// 403 + 빈 바디로 응답해버려서 프론트의 401 기반 refresh 로직이 아예 트리거되지 않는 문제가 있었음
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = new ErrorResponse("UNAUTHORIZED", "인증이 필요합니다.");
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
