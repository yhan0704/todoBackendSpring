# JWT 보안 개선 작업 정리 (2026-08-27)

이전 대화에서 "지금 JWT 구현이 충분히 강한 보안이냐"는 질문에 답하며 짚었던 문제들을 실제로 고친 기록입니다. 커밋/푸시 전에 뭘 왜 바꿨는지 남겨둡니다.

## 배경

`JWT.md`에 정리된 것처럼 로그인 → 토큰 발급 → 필터 인증까지는 되어 있었지만, 보안 관점에서 아래 4가지 구멍이 있었습니다.

1. JWT 시크릿 키가 소스코드에 하드코딩되어 git에 그대로 커밋됨
2. Access Token과 Refresh Token을 구분할 방법이 없어서, Refresh Token으로도 보호된 API를 호출할 수 있었음
3. Refresh Token을 탈취당하거나 로그아웃해도 서버가 무효화할 방법이 없었음 (30일간 계속 유효)
4. 로그인 실패 메시지가 "User not found" / "Invalid password"로 달라서 이메일 가입 여부가 노출됨 (user enumeration)

## 변경 내용

### 1. JWT 시크릿 환경변수 분리
- `src/main/resources/application.properties`에 `jwt.secret=${JWT_SECRET:<로컬 개발용 기본값>}` 추가
- `JwtUtil`이 `@Value`로 시크릿/만료시간(`jwt.access-token-expiration-ms`, `jwt.refresh-token-expiration-ms`)을 주입받도록 변경
- **운영 배포 전에는 반드시 `JWT_SECRET` 환경변수를 새로 생성한 값으로 설정해야 함** (properties의 값은 로컬 개발용 기본값일 뿐, 이 저장소를 본 사람은 누구나 알 수 있는 값)

### 2. Access/Refresh 토큰 타입 구분
- `JwtUtil`이 토큰 발급 시 payload에 `type: "access"` / `type: "refresh"` claim을 추가
- `validateToken()`을 `validateAccessToken()` / `validateRefreshToken()`으로 분리해 타입까지 검증
- `JwtAuthenticationFilter`는 이제 `validateAccessToken()`만 통과시킴 → Refresh Token으로는 API 인증 불가

### 3. Refresh Token 저장/회전(rotation)/무효화
- `User` 엔티티에 `refreshToken` 컬럼 추가
- 로그인 성공 시 발급한 Refresh Token을 `User.refreshToken`에 저장
- `POST /auth/refresh` 추가: 요청으로 들어온 Refresh Token이 (a) 서명·만료·타입이 유효하고 (b) DB에 저장된 값과 일치할 때만 새 Access/Refresh Token 발급. 발급할 때마다 Refresh Token을 새 값으로 교체(rotate)하므로 이전 토큰은 즉시 못 씀
- `POST /auth/logout` 추가: 전달받은 Refresh Token이 유효하고 DB 값과 일치하면 `User.refreshToken`을 `null`로 비움 → 로그아웃 이후 그 토큰으로는 재발급 불가

### 4. 로그인 실패 메시지 통일
- `AuthService.login()`에서 "이메일 없음"과 "비밀번호 틀림"을 모두 동일한 메시지(`"이메일 또는 비밀번호가 올바르지 않습니다."`)로 반환하도록 통일 → 이메일 가입 여부 추측 불가

## 변경/추가된 파일
- `src/main/java/backend/util/JwtUtil.java` — 시크릿/만료시간 외부 설정화, 토큰 타입 claim 및 검증 분리
- `src/main/java/backend/config/JwtAuthenticationFilter.java` — `validateAccessToken()` 사용하도록 변경
- `src/main/java/backend/user/entity/User.java` — `refreshToken` 필드 및 `updateRefreshToken()` 추가
- `src/main/java/backend/auth/service/AuthService.java` — `refresh()`, `logout()` 추가, 로그인 에러 메시지 통일
- `src/main/java/backend/auth/controller/AuthController.java` — `POST /auth/refresh`, `POST /auth/logout` 추가
- `src/main/java/backend/auth/dto/request/RefreshTokenRequest.java` — 신규 (refresh/logout 공용 요청 DTO)
- `src/main/resources/application.properties` — `jwt.secret`, `jwt.access-token-expiration-ms`, `jwt.refresh-token-expiration-ms` 추가
- `JWT.md` — 위 변경사항 반영해서 최신화

## 아직 안 한 것 (다음에 할 일)
`JWT.md` 8번 항목에 정리되어 있습니다. 요약하면:
- Todo ↔ User 연관관계
- 인증 실패 시 에러 응답 형식 통일 (`@RestControllerAdvice`) — 지금은 `RuntimeException`이 그대로 500으로 나감
- 로그인/재발급/로그아웃 로직에 `@Transactional` 미적용 (지금은 `save()` 명시 호출로 우회)
- 역할(Role) 기반 인가 없음 — 인증만 있고 인가는 없는 상태
