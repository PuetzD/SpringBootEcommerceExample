# Repository Guidelines

## Project Structure & Module Organization

This is a Spring Boot 4 / Java 21 modular monolith. Main code lives under `src/main/java/com/springbootecommerce/shophappens/`.

- `account/`, `customer/`, `catalog/`, `cart/`, `ordering/`: bounded contexts with `domain/`, `application/`, and `adapter/` layers.
- `security/`: Spring Security configuration and auth pages.
- `storefront/`: public web controllers.
- `shared/` and `sharedkernel/`: cross-cutting presentation support and shared value objects.
- `src/main/resources/db/migration/`: Flyway migrations.
- `src/main/resources/templates/`: Thymeleaf views.
- `src/main/resources/static/`: CSS and JavaScript assets.
- `src/test/java/`: unit, integration, and architecture tests.

Read `CONTEXT_MAP.md` and the relevant `contexts/<context>/CONTEXT.md` before changing a context package.

## Build, Test, and Development Commands

- `./mvnw test` - run the backend test suite.
- `./mvnw verify` - run formatting, static checks, unit tests, architecture tests, and integration tests.
- `./mvnw spring-boot:run` - start the app locally.
- `./mvnw spotless:apply` - format Java sources.
- `npm run build:css` - compile Tailwind CSS to `src/main/resources/static/css/output.css`.
- `npm run dev:css` - watch and rebuild CSS during UI work.
- `npm run build:frontend` - build CSS plus the admin frontend.

## Coding Style & Naming Conventions

Use Java 21, constructor-first Spring design, and small classes that stay inside their layer. Follow the package boundaries already present in nearby code. Keep names explicit and domain-based: `*Controller`, `*Service`, `*Repository`, `*IT`, `*Test`.

Java formatting is enforced by Spotless and Google Java Format. Do not hand-format generated CSS; rebuild it with the npm scripts.

## Testing Guidelines

Tests use JUnit 5, Spring Boot test support, MockMvc/web-slice tests, and Testcontainers for PostgreSQL and Redis. Integration tests follow the `*IT` suffix and typically extend `integration/AbstractIntegrationTest`.

Use the narrowest relevant test first, then expand to `./mvnw test` or `./mvnw verify` when behavior crosses layers or infrastructure.

## Commit & Pull Request Guidelines

Recent history uses short conventional-style commits such as `feat:` and `fix:`. Prefer that style for new work: `feat: add cart merge rule`.

Pull requests should include a short summary, the commands you ran, migration notes if schema changed, and screenshots for UI updates. Mention any security or auth impact explicitly.

## Security & Configuration Tips

Use Docker Compose for local PostgreSQL and Redis. Keep Flyway migrations append-only in shared environments, and avoid editing applied migrations once a release is shared. Sensitive config should stay in environment variables or local Compose overrides, not committed files.

## Agent Notes

Check `skills-lock.json` and `.agents/skills/` before making changes. Preserve unrelated worktree edits, make surgical changes, and verify the exact affected behavior before reporting completion.

<!-- CODEGRAPH_START -->
## CodeGraph

In repositories indexed by CodeGraph (a `.codegraph/` directory exists at the repo root), reach for it BEFORE grep/find or reading files when you need to understand or locate code:

- **MCP tool** (when available): `codegraph_explore` answers most code questions in one call — the relevant symbols' verbatim source plus the call paths between them, including dynamic-dispatch hops grep can't follow. Name a file or symbol in the query to read its current line-numbered source. If it's listed but deferred, load it by name via tool search.
- **Shell** (always works): `codegraph explore "<symbol names or question>"` prints the same output.

If there is no `.codegraph/` directory, skip CodeGraph entirely — indexing is the user's decision.
<!-- CODEGRAPH_END -->
