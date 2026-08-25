---
name: azure-containerization
description: Docker containerization and Azure deployment best practices for Java 21 (Spring Boot) applications. Use when creating Dockerfiles, docker-compose files, or configuring container deployments to Azure Container Apps, App Service, or AKS.
---

# Azure Containerization for Java 21 (Spring Boot)

Use this skill when containerizing Java 21 (Spring Boot) applications for Azure deployment.

## When to Use This Skill

- Creating Dockerfiles for Java 21 (Spring Boot) applications
- Setting up local development with Docker Compose
- Deploying to Azure Container Apps
- Deploying to Azure App Service (container)
- Deploying to Azure Kubernetes Service (AKS)

## Template Files

See the [templates](./templates/) directory for ready-to-use files:
- [Dockerfile](./templates/Dockerfile) - Multi-stage Java 21 Dockerfile
- [docker-compose.yml](./templates/docker-compose.yml) - Local development compose
- [.dockerignore](./templates/.dockerignore) - Files to exclude from build

## Best Practices

### 1. Use Multi-Stage Builds

Multi-stage builds reduce final image size by excluding build tools:

```dockerfile
# Build stage - includes JDK + Maven
FROM eclipse-temurin:21-jdk-alpine AS build
# ... build commands

# Runtime stage - smaller image
FROM eclipse-temurin:21-jre-alpine AS final
# ... only runtime files
```

### 2. Use Specific Image Tags

Never use `latest` in production:

```dockerfile
# ✅ Good - specific version
FROM eclipse-temurin:21-jre-alpine

# ❌ Bad - unpredictable
FROM eclipse-temurin:latest
```

### 3. Implement Health Checks

```dockerfile
HEALTHCHECK --interval=30s --timeout=10s --start-period=15s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1
```

### 4. Run as Non-Root User

```dockerfile
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
```

### 5. Use .dockerignore

Exclude unnecessary files to speed up builds and reduce image size.

### 6. Configure Proper Logging

Use stdout/stderr for container logs. In `application.yml`:

```yaml
logging:
  pattern:
    console: "%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n"
  level:
    root: INFO
```

### 7. Environment Variables for Configuration

```dockerfile
ENV SPRING_PROFILES_ACTIVE=production
ENV SERVER_PORT=8080
```

## Azure Container Apps Configuration

```yaml
# container-app.yaml
properties:
  configuration:
    ingress:
      external: true
      targetPort: 8080
    secrets:
      - name: spring-datasource-url
        value: ${SPRING_DATASOURCE_URL}
  template:
    containers:
      - image: myregistry.azurecr.io/myapp:latest
        name: myapp
        resources:
          cpu: 0.5
          memory: 1Gi
        probes:
          - type: liveness
            httpGet:
              path: /actuator/health/liveness
              port: 8080
          - type: readiness
            httpGet:
              path: /actuator/health/readiness
              port: 8080
```

## Azure App Service (Container) Configuration

```json
{
  "WEBSITES_PORT": "8080",
  "WEBSITES_JAVA_VERSION": "Java 21",
  "JAVA_SE_EMBEDDED_SERVER_ENABLED": "true",
  "DOCKER_REGISTRY_SERVER_URL": "https://myregistry.azurecr.io",
  "DOCKER_REGISTRY_SERVER_USERNAME": "myregistry",
  "DOCKER_REGISTRY_SERVER_PASSWORD": "@Microsoft.KeyVault(...)"
}
```

## ColdFusion to Container Migration Notes

| ColdFusion | Java Container |
|------------|----------------|
| CF engine on an app server (Adobe CF on Tomcat/JRun, Lucee/Railo on a servlet container) | Embedded Tomcat/Netty (Spring Boot) |
| CF Administrator settings / `Application.cfc` `this.*` | application.yml + env vars |
| `settings.ini.cfm` / CF Administrator | Environment variables / Spring profiles |
| File-storage directories on disk | Azure Blob Storage |
| CF `session` scope | Redis / Azure Cache |
| CF logs (`<cflog>` / server logs) | stdout → Azure Monitor |
| JVM args in `jvm.config` | `JAVA_TOOL_OPTIONS` / container JVM flags |
