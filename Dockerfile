FROM maven:3.9-eclipse-temurin-17-noble AS builder

WORKDIR /app

# Interim mitigation for CVE-2026-114938 (Java4Shell), also during Maven builds.
ENV JAVA_TOOL_OPTIONS="-XX:-UseBASE64Intrinsics"

# Build argument to control test execution (default: skip tests in Docker)
ARG RUN_TESTS=false

# Copy Maven wrapper and pom.xml
COPY pom.xml .

# Install dependencies to speed up build
RUN mvn dependency:resolve

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

# Keep the mitigation in JVM arguments so Compose JAVA_TOOL_OPTIONS cannot replace it.
ENTRYPOINT ["java", "-XX:-UseBASE64Intrinsics", "-jar", "app.jar"]
