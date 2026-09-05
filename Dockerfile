# Multi-stage Docker build for Mini Redis
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/mini-redis.jar app.jar

# Expose TCP Redis port (6379) and HTTP Web Dashboard port (8080)
EXPOSE 6379 8080

# Run standalone executable JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
