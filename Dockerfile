# syntax=docker/dockerfile:1.6
# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Cache deps first
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline

# Now the source
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests package

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S parches && adduser -S parches -G parches

COPY --from=build /workspace/target/*.jar app.jar
RUN chown parches:parches app.jar

USER parches

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
