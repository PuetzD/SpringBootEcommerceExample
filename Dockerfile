FROM node:22-alpine AS frontend

WORKDIR /app

COPY package.json package-lock.json ./
COPY admin-web/package.json ./admin-web/package.json
RUN npm ci

COPY admin-web/ ./admin-web/
COPY src/main/resources/static/css/ ./src/main/resources/static/css/
RUN npm run build:frontend

FROM maven:3.9.16-eclipse-temurin-21 AS build

WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn ./.mvn
RUN chmod +x mvnw

COPY src ./src
COPY --from=frontend /app/src/main/resources/static/ ./src/main/resources/static/
RUN ./mvnw -B -q -DskipTests package

FROM eclipse-temurin:21-jre

RUN apt-get update \
        && apt-get install --no-install-recommends --yes curl \
        && rm -rf /var/lib/apt/lists/* \
        && groupadd --system app \
        && useradd --system --gid app app

WORKDIR /app
COPY --from=build --chown=app:app /app/target/*.jar app.jar

USER app

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD curl --fail --silent http://localhost:8080/actuator/health/liveness || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
