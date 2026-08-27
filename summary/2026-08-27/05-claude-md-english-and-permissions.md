# Commit pending — Translate CLAUDE.md to English, add language rule, expand allowed git/gradle commands

- Translated the entire CLAUDE.md content into English (it was originally in Korean).
- Added a "Language rules" section: conversation with the user stays in Korean, but commit messages and feature descriptions (e.g. summary/ docs, PR descriptions) are written in English unless the user explicitly asks for Korean.
- Expanded `.claude/settings.local.json` allowed Bash commands: `git remote *`, `git push *`, `git config *`, `./gradlew compileJava` (without `-q`), to reduce permission prompts for routine git/build operations.
