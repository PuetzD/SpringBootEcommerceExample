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
- React 19, React Admin, and Vite for Catalog administration
- Maven, Docker Compose, and Testcontainers

## Current Architecture

The application is a modular monolith with separate `account`, `customer`,
`catalog`, `cart`, and `ordering` bounded contexts. Contexts collaborate through
application-port contracts rather than importing another context's domain or
persistence internals.

Checkout is intentionally a synchronous PostgreSQL consistency boundary. Order
creation, stock deduction, cart clearing, and creation of the corresponding
integration-outbox row commit or roll back together. Kafka is not required for a
checkout to succeed.

Successful checkouts produce the immutable, versioned
`ordering.order-placed.v1` event. The event contains identifiers and snapshots,
not JPA entities or mutable cart objects. Kafka publication is asynchronous and
opt-in; the default profile only persists the event in PostgreSQL.

The Kafka publisher currently provides bounded polling, broker-acknowledged
publication, event metadata headers, and retry-at-next-poll behavior. Delivery
is at least once: consumers must use the event ID for idempotency. A Kafka
consumer, inbox/processed-event store, and dead-letter workflow remain future
work.

## Running Locally

### Prerequisites

- Java 21
- Node 22
- Docker and Docker Compose

Use `./mvnw` for backend work, so you do not need a globally installed Maven.
Frontend dependencies and assets are managed separately with npm; the Docker
build composes the frontend and backend stages into the deployment image.

The simplest local setup on Windows, WSL, macOS, or Linux is:

1. Install Java 21 and Node 22.
2. Start PostgreSQL and Redis with Docker Compose.
3. Run the app with Maven from the host.

To run PostgreSQL, Redis, and the Java application in containers:

```bash
docker compose up --build
```

Open <http://localhost:8080> after the application starts. Stop the stack with
`docker compose down`.

The application uses PostgreSQL and Redis. When running the application directly
on the host, start those services first with Docker Compose:

```bash
docker compose up -d postgres redis
./mvnw spring-boot:run
```

Open <http://localhost:8080> after the application starts.

To run the containerized application with verbose development diagnostics:

```bash
SPRING_PROFILES_ACTIVE=dev docker compose up --build
```

By default the application logs quietly and hides SQL bindings. To opt into
verbose local diagnostics (Spring Security trace and Hibernate SQL/binding
logging), activate the `dev` profile:

```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

The `dev` profile runs schema migrations only. Demo data is deliberately not a
Flyway migration because it truncates application tables. To import it locally,
start the application once so Flyway creates the schema, stop it, and run these
Docker Compose commands from the repository root:

```text
docker compose up -d postgres
docker compose exec -T postgres psql -U demo -d demo -v ON_ERROR_STOP=1 -f /seed/demo-data.sql
```

The import command is the same on Windows, WSL, macOS, and Linux. It fails if
the schema has not been migrated and replaces the demo tables, so it is intended
only for a disposable local database. The command above uses the default Compose
credentials; if `POSTGRES_USER` or `POSTGRES_DB` is customized, use those values
in the `psql` command.

Run the backend test suite with:

```bash
./mvnw test
```

`./mvnw verify` runs the backend lifecycle, including formatting, Checkstyle,
PMD, unit tests, architecture tests, and integration tests. The integration
tests start disposable PostgreSQL and Redis
containers; Docker must be available for those tests. It also generates the JaCoCo coverage report at
`target/site/jacoco/index.html`.

Format Java sources with:

```bash
./mvnw spotless:apply
```

## Demo Data

The database schema is managed by Flyway migrations in `db/migration` and
checked against the JPA entities at startup via `ddl-auto: validate`:

- `V1__create_account_schema.sql` — accounts
- `V2__create_catalog_schema.sql` — categories and products
- `V3__create_cart_schema.sql` — customer carts
- `V4__create_ordering_schema.sql` — orders, checkout idempotency, and order query indexes
- `V5__create_integration_outbox.sql` — transactional integration events

The optional seed is maintained in `scripts/demo-data.sql`, outside Flyway's
migration locations. Use the Compose import command above instead of copying it
into a database manually.

Do not edit an applied migration in a shared environment. Add a new numbered
migration instead.

## Optional Kafka Publishing

Kafka publishing is disabled by default, so local checkout works without a
broker. To enable the publisher, provide a reachable Kafka broker and start the
application with:

```bash
ORDERING_EVENTS_KAFKA_ENABLED=true \
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092 \
./mvnw generate-resources spring-boot:run
```

The publisher reads unpublished rows from `integration_outbox`, sends them to
the `ordering.order-placed.v1` topic using the order ID as the Kafka key, and
marks a row published only after the broker acknowledges the send. The event
type, version, and event ID are included as Kafka headers. Producer idempotence
is enabled by default when Kafka is enabled. No Kafka service is included in
the default Compose stack; run one separately or use an environment-specific
Compose profile.

### Outbox inspection and recovery

An event-delivery failure does not roll back the already committed order. Check
the outbox before investigating the order transaction:

```sql
SELECT event_id, event_type, aggregate_key, created_at,
       published_at, attempt_count, last_error
FROM integration_outbox
WHERE published_at IS NULL
ORDER BY created_at ASC;
```

Rows with `published_at IS NULL` are retried by the publisher on a later poll.
Inspect `last_error` and `attempt_count`, restore broker connectivity or correct
the broker configuration, then restart or leave the publisher running. Do not
manually mark an event published unless the corresponding Kafka record has been
verified, because doing so can permanently suppress delivery.

If an order transaction fails, verify both the order and outbox counts using
the checkout ID. A rolled-back checkout must leave neither a customer-order row
nor an outbox row. If a broker send may have succeeded before the process failed,
assume at-least-once delivery and deduplicate downstream using `event_id`.

There is currently no automated dead-letter table or replay command. Preserve
the outbox row and its error metadata while investigating; destructive deletion
is not a recovery procedure.

## Frontend CSS

To rebuild Tailwind CSS directly:

```bash
npm run build:css
```

During UI development, watch for CSS changes with:

```bash
npm run dev:css
```

## Administration frontend

The `/admin` application uses React Admin resources for Catalog-owned Products
and flat Categories. It preserves the existing `/api/admin/products`,
`/api/admin/categories`, and `/api/admin/categories/options` contracts.

The typed Catalog data provider is the sole resource transport adapter. It
delegates to the shared API client, which owns same-origin credentials and the
`X-XSRF-TOKEN` CSRF header. Product and Category updates/deletes send the
current revision through `If-Match`; stale revisions and category-in-use
conflicts are surfaced without automatic retries.

Manage frontend dependencies and the production bundle separately from Maven:

```bash
npm ci
npm run test:admin
npm run build:admin
```

The generated bundle is written to `src/main/resources/static/admin/` and is
ignored by Git.

For frontend development with automatic rebuilds, run Spring Boot with the
Maven plugin's `addResources` configuration and start the admin bundle watcher
in separate terminals:

```bash
./mvnw spring-boot:run
npm run -w admin-web build:watch
```

The application serves resources directly from `src/main/resources`, so Java
does not need to be restarted after frontend changes. Run `npm run dev:css` in
an additional terminal when changing Tailwind input styles. This watch mode
reloads rebuilt assets on refresh; Vite's HMR development server is not used.

## AI Skills

This repository includes project-local AI agent skills under `.agents/skills/`.
They are recorded in `skills-lock.json`.

Restore the locked project skills with the [Skills CLI](https://skills.sh/):

```bash
npx skills experimental_install
```
