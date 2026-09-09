# Commit pending — Include todos in login response and scope todo listing to owner

## Why
- Login previously returned only the token and a bare profile, so rendering a first screen that needs the user's todos (e.g. a welcome dashboard) required a second round trip. Wiring `login()` to a fetch-joined query lets the response carry the caller's todos in one call.
- `TodoService.getAllTodos()`/`GET /todos` used `todoRepository.findAll()` with no ownership check, so any authenticated user's request returned every user's todos regardless of who was logged in. `GET /todos` now resolves the caller from the JWT and returns only their own todos; `getAllTodos()` is kept in the service (unused by any controller) for a possible future admin-only endpoint.

## What changed
- `User.java`: added `todos` (`@OneToMany(mappedBy = "user")`, lazy).
- `UserRepository.java`: added `findByEmailWithTodos` (`@EntityGraph(attributePaths = "todos")` + explicit `@Query`, since the derived-name parser misreads "WithTodos" as a nested property on `email`).
- `AuthService.login()`: switched from `findByEmail` to `findByEmailWithTodos`.
- `UserResponse.java`: added a `todos` field, mapped from `user.getTodos()` (empty list if null).
- `TodoRepository.java`: added `findByUser(User user)`.
- `TodoService.java`: added `getMyTodos()` — resolves the current user via `SecurityContextHolder` (same pattern as `createTodo()`) and returns only their todos via `findByUser`. `getAllTodos()` left as-is, no longer referenced by the controller.
- `TodoController.java`: `GET /todos` now calls `getMyTodos()` instead of `getAllTodos()`.

## Not done in this change
- `getTodo(id)` still has no ownership check — any authenticated user can fetch any todo by id. Deferred.
- No role/admin system exists yet, so `getAllTodos()` has no path to being exposed safely as an admin endpoint until that's built.
