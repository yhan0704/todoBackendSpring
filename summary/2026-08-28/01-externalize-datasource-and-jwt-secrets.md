# Commit pending — Externalize datasource and JWT secrets out of application.properties

## Why
`application.properties` was tracked in git (public GitHub repo) with real values for:
- `spring.datasource.username` / `spring.datasource.password`
- `jwt.secret` (as a literal fallback default for `${JWT_SECRET:...}`)

Since the repo is public, these were exposed in git history. The JWT secret in particular is a symmetric signing key — anyone with it could forge valid access tokens for any user.

## What changed
- `src/main/resources/application.properties`: replaced all real values with plain `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}`, `${JWT_SECRET}` placeholders (no literal fallback secret). Added `spring.profiles.active=${SPRING_PROFILES_ACTIVE:local}` so a plain local run picks up the `local` profile by default. Removed a corrupted/unreadable comment line.
- `src/main/resources/application-local.properties` (new, **gitignored**): holds the real local dev values. Kept the existing local Postgres credentials as-is (they're tied to a running local DB instance). Generated a **brand-new** `jwt.secret` — the previously committed one is treated as compromised and is not reused anywhere.
- `src/main/resources/application-local.properties.example` (new, tracked): placeholder template so the file can be recreated after a fresh clone. Documented in-file that `application-local.properties` must never be committed.
- `.gitignore`: added `application-local.properties`.

## Not done in this change
- The previously-leaked local DB password (`localdev1234`) was left unchanged since rotating it also requires an `ALTER USER` on the running local Postgres instance — left to the user to do if desired.
- Git history still contains the old leaked secrets (this commit doesn't rewrite history). Rotating the JWT secret value going forward is what neutralizes the leak; history cleanup was discussed as optional/separate.
