# Commit pending — Harden auth: structured errors, request validation, refresh-token hashing, audit logging

## Why
A code review of the auth flow (beyond the already-fixed duplicate-signup crash) found several remaining gaps:
- `login()`/`refresh()` threw raw `RuntimeException`, which isn't caught by `GlobalExceptionHandler`, so failed logins and invalid tokens returned 500 instead of 401.
- `SignupRequest`/`LoginRequest`/`RefreshTokenRequest` had no Bean Validation, even though `spring-boot-starter-validation` was already a dependency — blank/malformed input reached the service layer untouched.
- `User.refreshToken` was stored as plaintext; a DB leak would hand out working refresh tokens directly.
- `AuthService` had `@Slf4j` imported but no actual logging, so failed logins and invalid-token attempts left no audit trail.

## What changed
- `common/exception/InvalidCredentialsException.java`, `InvalidTokenException.java` (new): dedicated exception types for login and token failures.
- `common/exception/GlobalExceptionHandler.java`: added handlers — `InvalidCredentialsException` → 401, `InvalidTokenException` → 401, `MethodArgumentNotValidException` → 400 with joined field error messages.
- `AuthService.java`:
  - `login()` now throws `InvalidCredentialsException` for both "no such user" and "password mismatch", using the same message/exception type for both so the response can't be used to enumerate registered emails.
  - `refresh()`/`logout()` now throw/handle `InvalidTokenException` instead of raw `RuntimeException`.
  - `issueTokens()` now stores the refresh token hashed via the existing `BCryptPasswordEncoder` instead of plaintext; `refresh()`/`logout()` compare with `passwordEncoder.matches(...)` instead of `String.equals(...)`.
  - Added `log.warn` on login failure and invalid/expired/revoked refresh-token attempts, and `log.info` on successful signup, for brute-force detection and audit purposes.
- `SignupRequest.java`, `LoginRequest.java`, `RefreshTokenRequest.java`: added `@NotBlank`/`@Email`/`@Size` constraints.
- `AuthController.java`: added `@Valid` to all four endpoint request bodies so the above annotations actually run.
- `.claude/settings.local.json`: allow-listed the `gradlew.bat compileJava compileTestJava -q` command used to verify this change compiles.

## Not done in this change
- Login-attempt rate limiting was reviewed and intentionally deferred — it's usually handled at the infra/gateway level and was out of scope for this pass.
- Todo feature (missing ownership/authorization checks, input validation, exception handling, tests) was reviewed in the same session but explicitly deferred by the user to a later pass.
