# CLAUDE.md

This file is a project guideline that Claude Code automatically reads at the start of a session in this project.

## Required procedure before `git push`

Always follow these steps before running `git push`.

1. Check the `summary/<coding date, YYYY-MM-DD>/` folder. If today's date folder doesn't exist, create it.
2. Inside that folder, add a file describing what's being committed this time.
   - Filename: `NN-short-kebab-case-description.md` (NN is the next sequence number within that date folder, 2-digit zero-padded, and must match the commit order).
   - At the top of the file, write the title `# Commit <hash or "pending"> — <commit message summary>`.
   - Summarize what was changed and why in short bullet points. Even if the background explanation gets long, write it all in this file (don't scatter it elsewhere).
3. Then create the commit and push.

This procedure is performed automatically every time you push, without the user needing to request it each time.

## Language rules

- Converse with the user in Korean.
- However, commit messages and feature descriptions (e.g., documents inside the `summary/` folder, PR descriptions, etc.) are always written in English unless the user explicitly requests Korean.
- The user will write prompts in English. If a sentence is awkward or a backend term is used incorrectly, point out the corrected version before answering.
- Always respond to the user in Korean, regardless of the language the prompt was written in.
