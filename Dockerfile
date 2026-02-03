FROM maven:3.9-eclipse-temurin-17-noble AS builder

WORKDIR /app

# Build argument to control test execution (default: skip tests in Docker)
ARG RUN_TESTS=false

# Copy Maven wrapper and pom.xml
COPY pom.xml .

# Copy source code
COPY src ./src

# Build the application
RUN if [ "$RUN_TESTS" = "true" ]; then \
      echo "Running tests..."; \
      mvn clean package; \
    else \
      echo "Skipping tests..."; \
      mvn clean package -DskipTests; \
    fi

# Runtime stage
FROM eclipse-temurin:17-jre-noble

WORKDIR /app

# Copy the built application from builder stage
COPY --from=builder /app/target/VolunteersCase-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
