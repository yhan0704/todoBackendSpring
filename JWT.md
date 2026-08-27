# JWT 이해하기 (이 프로젝트 기준)

## 1. JWT가 뭔가요?

JWT = **JSON Web Token**. "로그인했다는 사실을 증명하는 카드"라고 생각하면 됩니다.

전통적인 로그인 방식(세션 로그인)은 이랬어요:

1. 로그인 성공 → 서버가 "세션"을 만들어서 서버 메모리/DB에 저장
2. 브라우저에는 `session_id`만 쿠키로 줌
3. 다음 요청부터 브라우저가 `session_id`를 보내면, 서버가 그 ID로 저장해둔 세션을 찾아서 "아 이 사람 로그인했구나" 확인

→ 이 방식은 서버가 **누가 로그인했는지 상태를 기억**해야 합니다 (stateful).

JWT는 반대입니다.

1. 로그인 성공 → 서버가 "이 사람은 email=abc@test.com 인 사람이 맞다"는 정보를 **토큰 안에 직접 넣고 서명**해서 클라이언트에 줌
2. 서버는 아무것도 저장 안 함
3. 다음 요청부터 클라이언트가 이 토큰을 헤더에 실어 보내면, 서버는 저장된 걸 찾는 게 아니라 **토큰 자체를 검증**해서 "이거 내가 서명한 게 맞네, 그럼 email=abc@test.com이 맞구나"라고 판단

→ 서버가 상태를 기억할 필요가 없습니다 (stateless). 이게 JWT를 쓰는 핵심 이유입니다.

## 2. JWT의 생김새

JWT는 이런 모양입니다. 점(`.`)으로 구분된 3개 부분입니다.

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhYmNAdGVzdC5jb20iLCJpYXQiOjE3MjQ...   .   4f8a2c...
   Header                        Payload                                Signature
```

### Header
```json
{ "alg": "HS256" }
```
어떤 알고리즘으로 서명했는지.

### Payload (Claims)
```json
{
  "sub": "abc@test.com",
  "iat": 1724700000,
  "exp": 1724703600
}
```
`sub`(subject) = 누구인지, `iat` = 발급 시각, `exp` = 만료 시각.

이 프로젝트의 `JwtUtil.generateAccessToken(email)`이 만드는 게 정확히 이 payload입니다.

### Signature
Header + Payload를 **서버만 아는 비밀키(secret key)**로 암호학적으로 서명한 값.

```
HMACSHA256(base64(header) + "." + base64(payload), secretKey)
```

## 3. 정말 중요한 오해 포인트: JWT는 "암호화"가 아니라 "서명"입니다

Header와 Payload는 **Base64로 인코딩만 되어 있을 뿐 암호화가 아닙니다**. 즉 누구나 https://jwt.io 같은 곳에 토큰을 붙여넣으면 payload 내용을 그대로 읽을 수 있습니다.

- ✅ 위변조는 막을 수 있음 → payload를 몰래 바꾸면 서명이 안 맞아서 검증 실패
- ❌ 내용을 숨길 수는 없음 → payload에 비밀번호, 주민번호 같은 민감정보를 넣으면 안 됨

이 프로젝트는 payload에 `email`만 넣고 있어서 이 부분은 안전합니다.

## 4. Access Token vs Refresh Token (이 프로젝트 기준)

`JwtUtil`을 보면 두 개를 발급합니다.

| | 용도 | 수명 | 이 프로젝트 값 |
|---|---|---|---|
| Access Token | 매 API 요청마다 "나 로그인했음" 증명 | 짧음 | 1시간 |
| Refresh Token | Access Token 만료 시 재발급용 | 김 | 30일 |

Access Token 수명을 짧게 두는 이유: 토큰이 탈취되더라도 피해 시간을 짧게 만들기 위해서입니다. 대신 매번 로그인하긴 귀찮으니, 오래 사는 Refresh Token으로 "새 Access Token 다시 줘"를 할 수 있게 하는 구조입니다.

> ⚠️ 업데이트: `/auth/refresh`(재발급), `/auth/logout`(무효화) 엔드포인트가 추가되었습니다. Refresh Token은 발급 시 `User.refreshToken` 컬럼에 저장되고, 재발급/로그아웃 때마다 그 값과 비교·회전(rotate)됩니다. 그래서 로그아웃하거나 재발급을 한 번 받으면 이전 Refresh Token은 즉시 무효가 됩니다 (탈취돼도 서버가 막을 수 있음). 또한 Access/Refresh Token 모두 payload에 `type` claim(`access`/`refresh`)을 넣어서, Refresh Token으로는 보호된 API를 호출할 수 없게 구분합니다.

## 5. 로그인 흐름 (지금 코드 기준)

```
[Client]                          [Server]
  |-- POST /auth/login ----------->|
  |   { email, password }          |  AuthService.login()
  |                                 |  1) email로 User 조회
  |                                 |  2) 비밀번호 matches 확인
  |                                 |  3) accessToken, refreshToken 생성
  |<-- { accessToken, refreshToken}-|
```

여기까지는 이미 잘 되어 있습니다. 문제는 그 다음입니다.

## 6. 왜 "JWT 필터 붙이기"가 필요한가

로그인해서 토큰을 받은 다음, 클라이언트는 보호된 API를 호출할 때 이렇게 보내야 합니다.

```
GET /todos
Authorization: Bearer eyJhbGciOi...
```

그런데 **서버가 이 헤더를 읽어서 검증하는 코드가 지금 어디에도 없습니다.**

`SecurityConfig`를 보면:

```java
.authorizeHttpRequests(auth -> auth
        .requestMatchers("/users/**", "/auth/**", "/todos/**").permitAll()
        .anyRequest().authenticated()
)
```

`/todos/**`, `/users/**`가 전부 `permitAll()`이라, Spring Security 입장에서는 "이 경로들은 로그인 여부 상관없이 통과"입니다. `JwtUtil.validateToken()`, `getEmail()` 메서드는 존재하지만 **어디에서도 호출되지 않고 있습니다.**

즉 지금 상태를 그림으로 그리면:

```
[Client] --GET /todos (토큰 없이 요청)--> [Server] --permitAll이라 그냥 통과--> [TodoController] → 200 OK
```

로그인 자체는 되지만, 로그인이 **아무 의미가 없는** 상태입니다. 토큰이 있든 없든, 유효하든 만료됐든 응답이 똑같습니다.

### 필터가 붙으면 무슨 일이 일어나는가

Spring Security는 요청이 컨트롤러에 도달하기 *전에* "필터 체인(Filter Chain)"을 거칩니다. 여기에 우리가 만든 `JwtAuthenticationFilter`를 끼워 넣으면:

```
[Client] --GET /todos, Authorization: Bearer <token>-->
   [JwtAuthenticationFilter]
      1) 헤더에서 토큰 꺼냄
      2) jwtUtil.validateToken(token) → 서명/만료 검증
      3) 유효하면 jwtUtil.getEmail(token) → "이 요청은 이 email이 보낸 것"이라고
         SecurityContext에 등록 (= 스프링 시큐리티에게 "이 사람 인증됨" 알림)
   [SecurityFilterChain의 authorizeHttpRequests 검사]
      - 토큰이 없거나 무효하면 → 인증 안 된 상태 → 401 반환하고 끝
      - 토큰이 유효하면 → 인증된 상태 → 통과
   [TodoController] → 실행
```

이렇게 되어야 비로소:
- 토큰 없이/잘못된 토큰으로 `/todos` 요청하면 401
- 유효한 토큰으로 요청하면 통과

가 실제로 동작합니다. 이게 되어야 다음 단계인 "내 todo만 보이게 하기(Todo-User 연관관계)"도 의미가 생깁니다 (지금 email이 누군지도 서버가 모르니까요).

## 7. 지금부터 코드로 하는 것

1. `JwtAuthenticationFilter` 추가 — 위에서 설명한 필터 로직
2. `SecurityConfig`에 이 필터를 등록 (`UsernamePasswordAuthenticationFilter` 앞에)
3. `/auth/**`만 `permitAll`로 남기고, `/users/**`, `/todos/**`는 인증 필요하도록 변경

## 8. 앞으로 남은 것 (오늘 안 하는 것)

- ~~Refresh Token 재발급 엔드포인트 (`/auth/refresh`)~~ 완료 — `/auth/refresh`, `/auth/logout` 추가, Refresh Token 회전/무효화 포함
- ~~JWT 시크릿 하드코딩 → `application.properties`로 분리~~ 완료 — `jwt.secret`을 `JWT_SECRET` 환경변수로 오버라이드 가능. **운영 배포 전에는 반드시 `JWT_SECRET` 환경변수를 새로 생성한 값으로 설정할 것** (properties의 값은 로컬 개발용 기본값일 뿐)
- Todo ↔ User 연관관계 (지금 Todo 엔티티엔 누가 만들었는지 필드가 없음)
- 인증 실패 시 에러 응답 형식 통일 (`@RestControllerAdvice`) — 지금은 `RuntimeException`이 그대로 500으로 나감
- 로그인 관련 로직에 `@Transactional` 미적용 — 현재는 `save()`를 명시 호출해서 우회 중
- 역할(Role) 기반 인가 없음 — `JwtAuthenticationFilter`가 항상 빈 권한 리스트로 인증 처리
