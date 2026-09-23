# Commit pending — Remove the unprotected PUT /users/{id}/email endpoint

## Why
- `PUT /users/{id}/email` took the target user from the path and never compared it to the caller. `SecurityConfig` only requires `authenticated()`, so any logged-in user could pass someone else's id and rewrite that account's email.
- That is worse than a normal IDOR read here, because the JWT subject *is* the email (`JwtUtil.buildToken` puts it in `sub`). Changing a victim's email immediately locks them out: their existing access token and stored refresh token both carry a subject that no longer resolves to a row, so every request fails until an admin fixes the database by hand.
- The endpoint was also unfinished in three other ways, which is why fixing it in place would have been a larger job than deleting it:
  - No `@Valid` on the controller argument and no constraints on `UpdateEmailRequest`, so `""`, `"asdf"` and `null` all passed through. `null` then violated `@Column(nullable = false)` and surfaced as a 500.
  - No duplicate-email check before the write. `DataIntegrityViolationException` happened to be mapped to 409 by `GlobalExceptionHandler`, but that is a last-resort net, not validation.
  - No refresh-token revocation and no current-password re-confirmation, both of which a real "change my login email" flow needs.
- Checked before deleting: nothing calls it. No reference to `/users` or `updateEmail` anywhere in `todoFrontEnd/src`, and no other backend class calls `UserService.updateEmail`.
- Same reasoning as `02-remove-unused-getallusers.md`: an unused, unprotected write endpoint is pure attack surface. When a real profile-editing feature is needed, it should be built as `PUT /users/me/email` resolving the user from `SecurityContextHolder`, the way `TodoService` already does.

## What changed
- `UserController`: removed `PUT /users/{id}/email` (`updateEmail`).
- `UserService`: removed the now-unused `updateEmail(Long, UpdateEmailRequest)`.
- Deleted `backend/user/dto/request/UpdateEmailRequest.java` (its only consumer is gone), leaving `user/dto/request/` empty and removed.
- `User`: removed the `updateEmail(String)` mutator, whose last caller was the deleted service method. `updateRefreshToken` is untouched.
- `compileJava` and `compileTestJava` both pass; no test referenced any of the removed code.

## Not done in this change
- `GET /users/{id}` still has no ownership check — any authenticated user can read any other user's name, email, nickname, phone, birth date and full todo list by guessing an id. Read-only, so lower severity than the write removed here, but it is the next thing to fix (replace with `/users/me`).
- `TodoService.getTodo(id)` has no ownership check either, unlike `editTodo` and `deleteTodo`, so a todo can be read by id across accounts.
- `UserService.getUserById` still throws bare `RuntimeException` with no matching handler in `GlobalExceptionHandler`, so a missing id answers 500 instead of 404.
- Broader auth hardening left open: no rate limiting on login, a 1-hour access token with no revocation path, plaintext emails in the login-failure logs, and tokens kept in `localStorage` on the frontend.
