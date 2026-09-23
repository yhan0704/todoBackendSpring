# Commit pending — Remove unused GET /users endpoint

## Why
- `UserController.getAllUsers()` exposed `GET /users`, returning every user's email, name, nickname, phone, birth date, and full todo list to any authenticated caller — no ownership or role check at all (`SecurityConfig` only requires `authenticated()`, which checks login status, not identity or privilege).
- Checked: nothing calls this endpoint. The frontend has no admin page and never requests `/users`; no other backend service calls `UserService.getAllUsers()` either. It's the same situation `TodoService.getAllTodos()` was already in — a method kept "in case an admin feature needs it later" — except `TodoController` never exposed that one as a route, while `UserController` did expose this one.
- Building real admin-only access control (a role field, seeding an admin user, checking the role) is a separate, bigger piece of work with no current consumer to justify it yet.

## What changed
- `UserController`: removed `GET /users` (`getAllUsers`).
- `UserService`: removed the now-unused `getAllUsers()` method.

## Not done in this change
- `GET /users/{id}` and `PUT /users/{id}/email` still have no ownership check — any authenticated user can read or change any other user's data by id, including changing another user's login email (the JWT subject), which locks that user out. Also unused by the frontend today, but higher severity than `getAllUsers` since it's a write. Left for a follow-up.
