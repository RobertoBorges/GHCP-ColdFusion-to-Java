# =============================================================================
# Multi-Stage Dockerfile for Java 21 (Spring Boot) Applications
# =============================================================================
# This Dockerfile uses multi-stage builds for smaller production images.
# Customize the project name and paths as needed.
# =============================================================================

# -----------------------------------------------------------------------------
# Stage 1: Build Stage
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy project descriptor and resolve dependencies (layer caching)
COPY pom.xml .
RUN mvn dependency:resolve dependency:resolve-plugins -B

# Copy remaining source code
COPY src ./src

# Build the application
RUN mvn package -DskipTests -B

# -----------------------------------------------------------------------------
# Stage 2: Final Production Image
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy built artifact from build stage
COPY --from=build --chown=appuser:appuser /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=production
ENV SERVER_PORT=8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Health check using Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=3s --start-period=15s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# Run as non-root user
USER appuser

# Entry point
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# =============================================================================
# Build Commands:
# =============================================================================
# Build:
#   docker build -t myapp:latest .
#
# Run:
#   docker run -d -p 8080:8080 --name myapp myapp:latest
#
# Run with environment variables:
#   docker run -d -p 8080:8080 \
#     -e SPRING_DATASOURCE_URL="jdbc:mysql://db:3306/mydb" \
#     -e SPRING_PROFILES_ACTIVE=development \
#     --name myapp myapp:latest
#
# Push to Azure Container Registry:
#   az acr login --name myregistry
#   docker tag myapp:latest myregistry.azurecr.io/myapp:latest
#   docker push myregistry.azurecr.io/myapp:latest
# =============================================================================
