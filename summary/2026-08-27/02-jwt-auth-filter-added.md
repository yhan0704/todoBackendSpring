# 커밋 a0e5809 — JWT 인증 필터 추가 및 SecurityConfig에 연결

- `JwtAuthenticationFilter` 추가: `Authorization: Bearer <token>` 헤더를 읽어 검증하고 `SecurityContext`에 인증 정보 등록
- `SecurityConfig`에 필터를 `UsernamePasswordAuthenticationFilter` 앞에 등록
- `/auth/**`만 `permitAll`, 나머지는 인증 필요하도록 변경

이때부터 토큰 없이/무효한 토큰으로 요청하면 401이 실제로 동작하기 시작함.
