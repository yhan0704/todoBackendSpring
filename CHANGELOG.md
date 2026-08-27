# Changelog

작업 내용을 **날짜별 → 그 안에서 커밋 순서별**로 기록합니다. push하기 전에 이 파일에 먼저 기록을 남기고, 그 다음 commit·push를 진행합니다. (규칙은 `CLAUDE.md` 참고)

## 2026-08-27

### 1. `01243aa` — 커밋1: JWT 필터 적용 전 상태
- JWT 필터 붙이기 전 기준점 커밋

### 2. `a0e5809` — 커밋2: JWT 인증 필터 추가 및 SecurityConfig에 연결
- `JwtAuthenticationFilter` 추가, `SecurityConfig`에 등록
- `/auth/**` 제외 나머지 경로 인증 필요하도록 변경

### 3. `1184553` — JWT 보안 강화: 시크릿 외부화, 토큰 타입 구분, refresh token 회전/무효화
- JWT 시크릿을 `JWT_SECRET` 환경변수로 분리 (기존: 소스코드 하드코딩)
- access/refresh 토큰에 `type` claim 추가, 필터는 access 토큰만 인증 허용
- `User.refreshToken`에 토큰 저장 → `/auth/refresh`, `/auth/logout`으로 회전·무효화 가능
- 로그인 실패 메시지 통일 (user enumeration 방지)
- 상세 배경: `JWT_SECURITY_CHANGES.md`

### 4. push 전 기록 절차 도입 (`CLAUDE.md`, `CHANGELOG.md` 추가)
- push하기 전 `CHANGELOG.md`에 날짜별·커밋순서별로 기록을 남기는 절차를 `CLAUDE.md`에 명시
- 앞으로는 이 파일을 먼저 갱신한 뒤 커밋·push한다
