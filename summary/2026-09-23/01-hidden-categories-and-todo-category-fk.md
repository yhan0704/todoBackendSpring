# Commit pending — Add hidden_categories table and switch Todo.category to a category_id FK

## Why
- Recommended categories are shared rows (`user_id IS NULL`). Letting a user delete one outright would remove it for every user, so there was no safe way for a user to stop seeing a recommended category they don't use.
- `Todo.category` was a free-text string copied from whatever category name existed at creation time. Renaming or deleting a category silently broke the link to any todo that had used that name — the todo just kept the old text forever, disconnected from the actual `Category` row.

## What changed
- New `HiddenCategory` entity + `HiddenCategoryRepository` (`hidden_categories` table, unique on `(user_id, category_id)`). Records "this user doesn't want to see this recommended category," without touching the shared original.
- `CategoryService.deleteCategory`: now branches on ownership —
  - `user_id IS NULL` (recommended) → inserts into `hidden_categories` instead of deleting the row.
  - owned by the caller → real delete, as before.
  - owned by someone else → `AccessDeniedException`, as before.
- `CategoryService.getMyCategories`: excludes any category id the current user has hidden.
- `Todo.category` (`String`) → `Todo.category` (`@ManyToOne Category`, `category_id` column). `TodoCreateRequest.category` → `categoryId`. `TodoResponse` now returns `categoryId` + `categoryName`.
- `TodoService.resolveCategory`: validates that a todo can only reference a recommended category or one the caller owns (same visibility rule as `GET /categories`), rejecting other users' personal categories.
- `data.sql`: categories are now seeded before todos (todos need `category_id` to resolve), and todo seed rows reference categories by id via a subquery instead of storing the name as text.

## Not done in this change
- Whether a regular user can *edit* a recommended category (as opposed to just hiding it) is still undecided — `editCategory` still blocks any category with `user_id IS NULL`.
- The old `todos.category` varchar column is not dropped (`ddl-auto=update` doesn't drop columns); it's just unused now.
