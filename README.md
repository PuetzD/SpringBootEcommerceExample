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
logging), activate the `dev` profile. This also seeds idempotent demo data from
`db.demo`:

```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

Run the backend test suite with:

```bash
./mvnw test
```

`./mvnw verify` runs the complete lifecycle, including CSS generation,
formatting, Checkstyle, PMD, unit tests, architecture tests, and Testcontainers
integration tests. The integration tests start disposable PostgreSQL and Redis
containers; Docker must be available for those tests.

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
- `V4__create_ordering_schema.sql` — orders and checkout idempotency
- `V5__create_integration_outbox.sql` — transactional integration events
- `V6__add_order_query_indexes.sql` — ownership and deterministic-history indexes

Do not edit an applied migration in a shared environment. Add a new numbered
migration instead.

## Optional Kafka Publishing

Kafka publishing is disabled by default, so local checkout works without a
broker. To enable the publisher, provide a reachable Kafka broker and start the
application with:

```bash
ORDERING_EVENTS_KAFKA_ENABLED=true \
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092 \
./mvnw spring-boot:run
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
