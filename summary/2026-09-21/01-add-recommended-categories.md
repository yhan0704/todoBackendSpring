# Commit pending — Add Category entity with shared recommended categories

## Why
- The frontend's category field was a free-text input backed by nothing on the backend. We designed (in conversation) a `Category` table where a nullable `user_id` distinguishes "recommended" categories (shared by everyone, `user_id IS NULL`) from a future "user-created" category (owned by exactly one user), mirroring the existing nullable `Todo.user` pattern used for the ownerless-todo practice row in `data.sql`.
- This change implements only the recommended-category half of that design: a `GET /categories` endpoint that returns the 10 seeded shared categories. Creating/editing/deleting personal categories is intentionally not built yet.

## What changed
- New `backend.category` package, following the same layout as `backend.todo`:
  - `Category` entity: `id`, `name`, nullable `@ManyToOne User user`.
  - `CategoryRepository`: `findByUserIsNullOrUser(User user)` (Spring Data derived query).
  - `CategoryService.getMyCategories()`: resolves the current user from `SecurityContextHolder` (same pattern as `TodoService.getMyTodos()`) and returns recommended + own categories.
  - `CategoryController`: `GET /categories`, no `SecurityConfig` change needed since all non-`/auth/**` routes already require authentication.
- `data.sql`: seeded 10 recommended categories (건강, 요리, 업무, 쇼핑, 운동, 공부, 집안일, 여행, 재정, 자기계발) with `user_id NULL`, using the same `NOT EXISTS` idempotency pattern as the existing todo seed rows.

## Not done in this change
- `Todo.category` is still a free-text `String`, not a FK to `Category`. Wiring todos to reference `category_id`, and letting users create/edit/delete their own categories, are planned as follow-up work.
- Frontend still uses a hardcoded mock category list; it hasn't been switched to call `GET /categories` yet.
