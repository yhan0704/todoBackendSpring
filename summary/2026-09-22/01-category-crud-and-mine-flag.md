# Commit pending — Add category create/edit/delete and a `mine` flag on category responses

## Why
- Users need to be able to manage their own categories (not just read the shared recommended list), so the frontend can offer add/edit/delete on personal categories.
- The frontend also needs to tell recommended (shared) categories apart from the current user's own categories, to decide which ones can show edit/delete controls. `CategoryResponse` previously only returned `id` and `name`, giving the frontend no way to know ownership without guessing.

## What changed
- `CategoryController`: added `POST /categories`, `PUT /categories/{id}`, `DELETE /categories/{id}`, alongside the existing `GET /categories`.
- `CategoryService`:
  - `createCategory`: creates a category owned by the current user.
  - `editCategory` / `deleteCategory`: look up the category, then call `requireOwnCategory` before mutating.
  - `requireOwnCategory`: throws `AccessDeniedException` unless the category's `user_id` matches the current user — this also blocks editing/deleting recommended categories (`user_id IS NULL`), since nobody owns those.
- `CategoryResponse`: added a `mine` boolean field. `from(Category, Long currentUserId)` now computes `mine = category.getUser() != null && category.getUser().getId().equals(currentUserId)`. All call sites in `CategoryService` were updated to pass the current user's id.

## Not done in this change
- Recommended (shared) categories still cannot be edited, deleted, or "hidden" by a user — that design (a `HIDDEN_CATEGORY` table for per-user hiding of recommended categories) was discussed but not implemented yet.
- `Todo.category` is still a free-text `String`, not a `category_id` FK — still pending, as noted in earlier summaries.
