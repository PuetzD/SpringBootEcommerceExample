# Spring Boot Ecommerce Example

This is a learning project for building an ecommerce application with Java and
Spring Boot. It is intentionally developed in small steps to explore practical
application structure, security, persistence, testing, and server-rendered UI
development.

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

Run the backend test suite with:

```bash
./mvnw test
```

Format Java sources with:

```bash
./mvnw spotless:apply
```

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
