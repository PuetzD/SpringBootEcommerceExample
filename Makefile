.DEFAULT_GOAL := help

.PHONY: help up down logs run seed-demo test verify format frontend css admin-test

help:
	@printf '%s\n' \
		'Usage: make <target>' \
		'' \
		'  up          Start PostgreSQL, Redis, and Kafka' \
		'  down        Stop the Compose stack' \
		'  logs        Follow application container logs' \
		'  run         Run Spring Boot on the host' \
		'  seed-demo   Replace the local database with themed demo data' \
		'  test        Run backend tests' \
		'  verify      Run backend verification and integration tests' \
		'  format      Format Java sources' \
		'  frontend    Build CSS and the admin frontend' \
		'  css         Build Tailwind CSS' \
		'  admin-test  Run admin frontend tests'

up:
	docker compose up -d postgres redis kafka mailpit

down:
	docker compose down

logs:
	docker compose logs -f app

run:
	./mvnw spring-boot:run

seed-demo:
	docker compose exec -T postgres psql -U "$${POSTGRES_USER:-demo}" -d "$${POSTGRES_DB:-demo}" -v ON_ERROR_STOP=1 -f /seed/demo-data.sql

test:
	./mvnw test

verify:
	./mvnw verify

format:
	./mvnw spotless:apply

frontend:
	npm run build:frontend

css:
	npm run build:css

admin-test:
	npm run test:admin
