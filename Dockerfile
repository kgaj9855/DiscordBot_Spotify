# syntax=docker/dockerfile:1

FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /app

# 先複製 pom，讓依賴下載層可以被 Docker 快取。
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B package -DskipTests


FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# 不使用 root 身分執行服務。
RUN addgroup -S app && adduser -S app -G app

COPY --from=build --chown=app:app \
    /app/target/DiscordBot-1.0-SNAPSHOT.jar ./app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
