# Commit pending — Add backend README summarizing project work

## Why
- Backend had no `README.md` (only `HELP.md`, `JWT.md`, `top1Tier.md`), and there was no single place summarizing what was built across both the backend and frontend repos for someone landing on the project.

## What changed
- Added `README.md` with a short overview of the stack and a bullet summary of the work done: JWT auth (access/refresh rotation, hashing), scoped todo CRUD with ownership checks, category feature (recommended + user-owned, hidden categories, `category_id` FK), endpoint cleanup/hardening, and the corresponding frontend auth/todo/category UI.
- Points to `summary/<date>/` for per-change detail instead of duplicating it in the README.

## Not done in this change
- Unrelated local working-tree changes (deleted `UserController.java` / `UserService.java`, from the `PUT /users/{id}/email` removal) were left untouched and are not part of this commit.
