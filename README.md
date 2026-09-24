# Todo Fullstack

A full-stack todo app: Spring Boot backend + Next.js frontend, with JWT-based auth and per-user data ownership.

- Backend: Java, Spring Boot, Spring Security, JPA/Hibernate
- Frontend: Next.js (TypeScript) — see [`todoFrontEnd/README.md`](../todoFrontEnd/README.md)

## What I built

**Auth**
- JWT login/signup with access + refresh tokens, refresh token hashing and rotation
- Global exception handling and request validation, duplicate-signup prevention
- Secrets externalized out of `application.properties`

**Todos**
- CRUD endpoints scoped to the logged-in user (ownership checks on edit/delete/list)
- Todo–User (`@ManyToOne`) and Todo–Category relations

**Categories**
- Shared "recommended" categories plus user-owned custom categories
- Create/edit/delete endpoints with a `mine` flag on responses
- Hidden-categories support (per-user hide list) and `Todo.category` migrated to a `category_id` FK

**Cleanup / hardening**
- Removed unused/unprotected endpoints (`GET /users`, `PUT /users/{id}/email`) found while reviewing auth coverage
- 401 vs 403 correctness for unauthenticated vs unauthorized requests

**Frontend**
- Auth flow, todo CRUD UI, and category create/edit/delete UI
- Category selection switched from a free-text name to a `categoryId` FK to match the backend

See `summary/<date>/` for the detailed per-change history.
