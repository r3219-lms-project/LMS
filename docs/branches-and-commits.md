# Git Branches and Commits

**Documentation Author:** Bykov Lev  
**Last Updated:** December 9, 2025  
**Version:** 1.0.0

## Overview

This document explains how to name branches and how to write commit messages for the LMS project.

---

## Branch naming

We use Jira task keys in all branch names.

General format:

```bash
<prefix>/LMS-<issue-number>-<kebab-case-title>
```

Where:

- `<prefix>` is usually:
    - `feature` — new features
    - `bugfix` — bug fixes
    - `hotfix` — urgent fixes for production
    - `chore` — tech tasks, refactoring, maintenance
- `<issue-number>` is the number from Jira
- `<kebab-case-title>` is a short task title in lowercase with `-` between words

Examples:

```bash
feature/LMS-49-update-docs
feature/LMS-102-add-refresh-token-rotation
bugfix/LMS-87-fix-user-registration
hotfix/LMS-130-fix-production-config
chore/LMS-140-update-dependencies
```


### Branch workflow

1. Take a task in Jira (for example `LMS-49`).
2. Create a branch from `main`:

```bash
git checkout main
git pull
git checkout -b feature/LMS-49-update-docs
```


3. Make changes and commits in this branch.
4. Push the branch:

```bash
git push -u origin feature/LMS-49-update-docs
```


5. Create a Pull Request from this branch into `main` and link the Jira task.

After merge you can delete the branch:

```bash
git branch -d feature/LMS-49-update-docs
git push origin --delete feature/LMS-49-update-docs
```


---

## Commit messages

We also use Jira task keys in commit messages.

General format:

```bash
LMS-<issue-number>: <short-description>
```


Rules:

- Always start with `LMS-<number>`.
- Use a short and clear description.
- Use imperative form (for example: `add`, `fix`, `update`).

Examples:

```bash
LMS-49: add backend setup docs
LMS-73: fix authservice test profile
LMS-102: split CI jobs for H2 services
```

Use bullets to list important changes.

### Recommendations

- One logical change = one commit.
- Do not use messages like `fix`, `wip`, `tmp` without a Jira key.
- Try to keep commits small and focused.
- Use English for commit messages to keep them consistent.
