# Commit pending — Add ownership check to deleteTodo and deny Bash-based reads of application-local.properties

## Why
- `TodoService.deleteTodo(id)` called `todoRepository.deleteById(id)` directly with no ownership check, unlike `editTodo`, which already verifies the requester owns the todo before mutating it. Any authenticated user could delete any other user's todo by id. Added the same owner check used in `editTodo` so only the owning user can delete their own todo.
- `.claude/settings.json` denied `Read(**/application-local.properties)`, but that pattern only gates the `Read` tool. The same file could still be opened via `Bash(cat ...)` or PowerShell `Get-Content`, bypassing the intended protection. Added matching `Bash` deny patterns to close that gap.

## What changed
- `TodoService.java`: `deleteTodo(id)` now resolves the current user from `SecurityContextHolder` (same pattern as `editTodo`/`createTodo`), loads the `Todo`, and throws `AccessDeniedException` if the caller doesn't own it, before calling `todoRepository.deleteById(id)`.
- `.claude/settings.json`: added `Bash(cat **/application-local.properties)` and `Bash(*Get-Content*application-local.properties*)` to the `deny` list.

## Not done in this change
- Other Bash-based read paths (`type`, `more`, `Select-String`, scripting via Python/Node, etc.) are still not blocked by settings deny rules. Full protection would require keeping the file entirely out of the working directory (e.g. `.gitignore` + local-only).
