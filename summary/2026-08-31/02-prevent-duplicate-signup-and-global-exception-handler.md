# Commit pending — Prevent duplicate signup and add global exception handler foundation

## Why
Signing up twice with the same email created two rows in `users` (no unique constraint, no application-level check). Any later `login()` call for that email then threw `IncorrectResultSizeDataAccessException` from `UserRepository.findByEmail` (which expects a single result), returning a raw 500 and permanently breaking login for that account. There was also no `@RestControllerAdvice` anywhere, so every thrown exception fell through to Spring's default error handling.

## What changed
- `User.java`: added `@Column(nullable = false, unique = true)` on `email` as a DB-level backstop.
- `AuthService.signup()`: checks `userRepository.findByEmail(email).isPresent()` before saving and throws `EmailAlreadyExistsException` if the email is taken.
- `common/exception/EmailAlreadyExistsException.java`, `ErrorResponse.java` (new): custom exception + a `{code, message}` response shape.
- `common/exception/GlobalExceptionHandler.java` (new): maps `EmailAlreadyExistsException` → 409, and `DataIntegrityViolationException` → 409 as a race-condition backstop for two concurrent signups that both pass the `isPresent()` check before either commits.
- `build.gradle`, `BackendApplicationTests.java`: added Testcontainers (Postgres) so integration tests run against a real Postgres instance instead of H2, matching the `unique` constraint behavior used in prod.
- `test/auth/service/AuthServiceTest.java`, `test/auth/controller/AuthControllerIntegrationTest.java` (new): unit tests for `signup()` (normalization, duplicate rejection) and an integration test covering the concurrent-signup race condition.
- `test/resources/application.properties` (new): test profile config — Testcontainers-supplied datasource, test-only JWT secret.
