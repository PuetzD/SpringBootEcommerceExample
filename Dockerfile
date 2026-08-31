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

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
