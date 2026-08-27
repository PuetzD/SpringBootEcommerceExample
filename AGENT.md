# Agent Guide

## Project

Spring Boot 4 / Java 21 ecommerce application with Thymeleaf, Spring Security,
JPA, PostgreSQL/Flyway, Tailwind CSS 4, and daisyUI.

## Layout

- `src/main/java/com/springbootecommerce/demo/`
  - `account/`: account domain, queries, persistence
  - `security/`: authentication, authorization, login web endpoints
  - `storefront/`: public storefront controllers
- `src/main/resources/`
  - `db/migration/`: pre-release Flyway baseline; keep V1 onward clean and
    coherent by editing or renumbering migrations while local database resets
    remain acceptable. Freeze applied migrations once a release or shared
    persistent environment exists.
  - `templates/`: Thymeleaf views
  - `static/`: CSS and JavaScript assets
- `src/test/java/`: unit and integration tests; `*IT` tests use PostgreSQL
  Testcontainers.

## Commands

- `./mvnw test` - run backend tests.
- `./mvnw verify` - run the full Maven verification lifecycle.
- `npm run build:css` - build `static/css/output.css`.
- `npm run dev:css` - watch and rebuild CSS while developing UI.

Use the narrowest relevant existing test first. Do not install dependencies
unless a required command reports they are missing.

## Conventions

- Keep application concerns in their existing package layer; favor small,
  focused classes.
- Use Spring MVC and Thymeleaf patterns already present in nearby code.
- Preserve security defaults; cover authorization or authentication changes
  with tests.
- Use Flyway for schema changes and test against the relevant database path.
- Run formatting through the configured Maven lifecycle; do not hand-format
  generated CSS.
- Keep the current worktree's unrelated changes intact. Never reset, revert,
  or amend unless explicitly asked.

## Agent Workflow

- Check `skills-lock.json` and `.agents/skills/` before work. Invoke a relevant
  installed skill before acting when its trigger applies.
- Prefer direct, targeted inspection and edits; use subagents only for work
  that benefits from independent, substantial context.
- State assumptions only when they affect behavior. Keep output concise and
  avoid repeating the request or routine tool narration.
- Make surgical changes, reuse existing patterns, and add or update focused
  tests for behavioral changes.
- Verify the exact affected behavior before reporting completion. Do not claim
  success on unrun or failing checks.
