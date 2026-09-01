# Commit pending — Add discussion-vs-implementation rule to CLAUDE.md

## Why
Claude was creating/editing files when the user was only asking questions or discussing an idea, instead of waiting for an explicit implementation request. This rule makes the expected workflow explicit for future sessions.

## What changed
- `CLAUDE.md`: added a "Discussion vs. implementation" section — only explain/discuss when the user is asking questions or confirming something; only start editing files once the user explicitly asks (e.g. "구현해줘", "적용해줘", "고쳐줘"); ask first if a short reply is ambiguous between agreement and authorization.
