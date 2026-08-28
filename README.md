# Spring Boot Ecommerce Example

This is my learning project for building an ecommerce application with Java and
Spring Boot. It is intentionally developed in small steps to explore practical
application structure, security, persistence, testing, and server-rendered UI
development. I'm also learning DDD here.

## Technology

- Java 21 and Spring Boot 4
- Spring MVC, Thymeleaf, and Spring Security
- Spring Data JPA, PostgreSQL, Flyway, and Redis
- Tailwind CSS and daisyUI
- Maven, Docker Compose, and Testcontainers

## Planned Direction

The application currently has a public storefront and authentication foundations.
Future learning milestones may include:

- Product catalogue and product detail pages
- Customer accounts and role-based administration
- Shopping cart and checkout workflows
- Order management and supporting ecommerce operations
- Event-driven architecture with Kafka (async communication between components)

The scope will evolve as the project is used to learn more of the Spring Boot
ecosystem.

## Running Locally

### Prerequisites

- Java 21
- Docker and Docker Compose

The application uses PostgreSQL and Redis. Spring Boot starts the services in
`docker-compose.yml` automatically when Docker is available.

```bash
./mvnw spring-boot:run
```

Open <http://localhost:8080> after the application starts.

To manage the supporting services manually instead:

```bash
docker compose up -d
./mvnw spring-boot:run
```

By default the application logs quietly and hides SQL bindings. To opt into
verbose local diagnostics (Spring Security trace and Hibernate SQL/binding
logging), activate the `dev` profile:

```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

Run the backend test suite with:

```bash
./mvnw test
```

Format Java sources with:

```bash
./mvnw spotless:apply
```

## Demo Data

The database schema is managed by Flyway migrations in `db/migration`
(`V1__create_account_schema.sql`, `V2__create_catalog_schema.sql`) and checked
against the JPA entities at startup via `ddl-auto: validate`.

A repeatable Flyway migration in `db/demo/R_demo_catalog.sql` seeds demo catalog
data (categories and sample products) so the storefront has something to show.
Like all schema migrations it runs automatically on startup, since `db/demo` is
configured in `spring.flyway.locations`. Note that repeatable migrations re-run
whenever their content changes and the seed file is not idempotent, so editing
it after it has already been applied can cause duplicate rows.

## Frontend CSS

The Maven build installs the frontend dependencies and builds CSS
automatically. To rebuild Tailwind CSS directly:

```bash
npm run build:css
```

During UI development, watch for CSS changes with:

```bash
npm run dev:css
```

## AI Skills

This repository includes project-local AI agent skills under `.agents/skills/`.
They are recorded in `skills-lock.json`.

Restore the locked project skills with the [Skills CLI](https://skills.sh/):

```bash
npx skills experimental_install
```
