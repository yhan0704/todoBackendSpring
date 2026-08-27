# 커밋 (예정) — push 기록 방식을 summary/ 폴더로 변경

- 처음엔 `CHANGELOG.md` 한 파일에 날짜 헤더 + 커밋 번호로 몰아서 기록하는 방식을 썼는데, `summary/<날짜>/<번호>-<설명>.md` 형태로 커밋마다 별도 파일을 두는 방식으로 변경
- `CHANGELOG.md` 삭제, `JWT_SECURITY_CHANGES.md`는 `summary/2026-08-27/03-jwt-secret-and-refresh-token-security.md`로 이동
- 앞으로 push 전에는 `summary/<코딩한 날짜>/` 폴더 아래에 그 커밋 내용을 설명하는 파일을 하나 추가한다 (규칙은 `CLAUDE.md` 참고)
