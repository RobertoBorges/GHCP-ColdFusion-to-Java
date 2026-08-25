---
name: spring-boot-project-structure
description: Spring Boot 3.x project structure and scaffolding templates for migrated PHP applications. Use when creating new Spring Boot projects, setting up folder structure, or generating boilerplate code for Spring MVC or REST APIs.
---

# Spring Boot 3 Project Structure Guide

> Use this skill when setting up the Spring Boot 3.x project structure for migrated PHP applications.

## Recommended Project Structure

### Spring Boot MVC Project (from Laravel/Symfony)

```
my-app/
├── pom.xml                                  # Maven build (or build.gradle)
├── src/
│   ├── main/
│   │   ├── java/com/example/app/
│   │   │   ├── Application.java             # @SpringBootApplication entry point
│   │   │   ├── config/                      # Configuration classes
│   │   │   │   ├── ApplicationConfig.java   # General app config / beans
│   │   │   │   ├── SecurityConfig.java      # Spring Security setup
│   │   │   │   ├── CacheConfig.java         # Cache configuration
│   │   │   │   └── WebMvcConfig.java        # MVC customizations
│   │   │   ├── controller/                  # MVC + REST controllers
│   │   │   │   ├── HomeController.java
│   │   │   │   ├── ProductController.java
│   │   │   │   └── api/                     # REST API controllers
│   │   │   │       └── ProductApiController.java
│   │   │   ├── service/                     # Business logic
│   │   │   │   ├── ProductService.java
│   │   │   │   └── UserService.java
│   │   │   ├── repository/                  # Spring Data JPA repositories
│   │   │   │   ├── ProductRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── entity/                      # JPA entities (from Eloquent models)
│   │   │   │   ├── Product.java
│   │   │   │   └── User.java
│   │   │   ├── dto/                         # Data Transfer Objects (Java records)
│   │   │   │   ├── CreateProductRequest.java
│   │   │   │   └── ProductResponse.java
│   │   │   ├── exception/                   # Custom exceptions + global handler
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── security/                    # Security components
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   └── JwtTokenProvider.java
│   │   │   └── util/                        # Utility classes
│   │   │       └── SlugUtil.java
│   │   └── resources/
│   │       ├── application.yml              # Main configuration
│   │       ├── application-dev.yml          # Dev profile overrides
│   │       ├── application-prod.yml         # Prod profile overrides
│   │       ├── templates/                   # Thymeleaf templates (from Blade/Twig)
│   │       │   ├── layout/
│   │       │   │   └── main.html            # Base layout
│   │       │   ├── fragments/
│   │       │   │   ├── header.html
│   │       │   │   └── footer.html
│   │       │   ├── home/
│   │       │   │   └── index.html
│   │       │   └── products/
│   │       │       ├── index.html
│   │       │       └── show.html
│   │       ├── static/                      # Static assets (CSS, JS, images)
│   │       │   ├── css/
│   │       │   ├── js/
│   │       │   └── images/
│   │       └── db/
│   │           └── migration/               # Flyway SQL migrations
│   │               ├── V1__create_users_table.sql
│   │               └── V2__create_products_table.sql
│   └── test/
│       └── java/com/example/app/
│           ├── controller/
│           │   └── ProductControllerTest.java
│           ├── service/
│           │   └── ProductServiceTest.java
│           └── repository/
│               └── ProductRepositoryTest.java
```

### Spring Boot REST API Project (from Slim/Lumen)

```
my-api/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/example/api/
│   │   │   ├── Application.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── OpenApiConfig.java      # Swagger/OpenAPI docs
│   │   │   ├── controller/                  # REST controllers only
│   │   │   │   ├── ProductController.java
│   │   │   │   └── UserController.java
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── entity/
│   │   │   ├── dto/
│   │   │   └── exception/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   └── test/
```

## Template Files

See the [templates](./templates/) directory for starter files:
- [Application.java](./templates/Application.java) - Spring Boot entry point
- [ApplicationConfig.java](./templates/ApplicationConfig.java) - Configuration with beans
- [BaseController.java](./templates/BaseController.java) - Base controller utilities
- [SecurityConfig.java](./templates/SecurityConfig.java) - Spring Security setup

## PHP to Spring Boot Folder Mapping

| PHP (Laravel) | Spring Boot (Java) |
|---------------|--------------------|
| `app/Http/Controllers/` | `controller/` |
| `app/Http/Controllers/Api/` | `controller/api/` or `controller/` with `@RestController` |
| `app/Models/` | `entity/` |
| `app/Services/` | `service/` |
| `app/Repositories/` | `repository/` (Spring Data JPA interfaces) |
| `app/Http/Requests/` | `dto/` (request records with validation annotations) |
| `app/Http/Resources/` | `dto/` (response records) |
| `app/Exceptions/` | `exception/` |
| `app/Http/Middleware/` | `config/` (Filters/Interceptors) or `security/` |
| `app/Providers/` | `config/` (`@Configuration` classes) |
| `resources/views/` | `resources/templates/` (Thymeleaf) |
| `resources/views/layouts/` | `resources/templates/layout/` |
| `resources/views/components/` | `resources/templates/fragments/` |
| `public/` | `resources/static/` |
| `routes/web.php` | `@Controller` class annotations (`@GetMapping`, etc.) |
| `routes/api.php` | `@RestController` class annotations |
| `config/` | `application.yml` + `@ConfigurationProperties` classes |
| `database/migrations/` | `resources/db/migration/` (Flyway SQL) |
| `database/seeders/` | `CommandLineRunner` beans or Flyway callbacks |
| `storage/` | External storage (S3, Azure Blob) or `/var/data/` |
| `.env` | `application.yml` + environment variables |
| `composer.json` | `pom.xml` (Maven) or `build.gradle` (Gradle) |
| `tests/` | `src/test/java/` |

## Java Naming Conventions

| Concept | PHP | Java |
|---------|-----|------|
| Files | `snake_case.php` or `PascalCase.php` | `PascalCase.java` |
| Classes | `PascalCase` | `PascalCase` |
| Methods | `camelCase` | `camelCase` |
| Variables | `$camelCase` | `camelCase` |
| Properties/Fields | `$snake_case` or `$camelCase` | `camelCase` |
| Constants | `UPPER_SNAKE_CASE` | `UPPER_SNAKE_CASE` (static final) |
| Packages | N/A (namespaces) | `lowercase.dot.separated` |
| Interfaces | `Interface` suffix | No prefix/suffix convention (e.g., `ProductService`) |
| Enums | `PascalCase` | `PascalCase` (values: `UPPER_SNAKE_CASE`) |
| DTOs | Associative arrays or classes | Java `record` types (Java 17+) |
| Test classes | `PascalCaseTest` | `PascalCaseTest` |

## Key Differences from .NET Structure

| .NET Convention | Java Convention |
|----------------|-----------------|
| `PascalCase` methods | `camelCase` methods |
| `IService` interface prefix | No prefix — `ProductService` (interface) / `ProductServiceImpl` (impl) or just `ProductService` (class) |
| `appsettings.json` | `application.yml` or `application.properties` |
| `Program.cs` top-level | `Application.java` with `main()` method |
| `.csproj` | `pom.xml` (Maven) or `build.gradle` (Gradle) |
| NuGet packages | Maven Central / Gradle dependencies |
| `Startup.cs` / `Program.cs` DI | `@Configuration` + `@Bean` |
| `namespace` | `package` |

## Best Practices

1. **One class per file** — Each `.java` file contains one public class (Java requirement)
2. **Constructor injection** — Prefer constructor injection over `@Autowired` field injection
3. **Interface-based services** — Optional but recommended for testability; Spring proxies work without interfaces too
4. **DTOs as records** — Use Java `record` types for request/response DTOs (immutable, concise)
5. **@ConfigurationProperties** — Use for typed configuration instead of scattered `@Value` annotations
6. **Profile-specific config** — Use `application-{profile}.yml` for environment differences
7. **Package by feature** — For large projects, consider packaging by feature instead of by layer
8. **Async everywhere** — Use `@Async` for non-blocking operations, `CompletableFuture` for async returns
9. **Global exception handling** — Use `@RestControllerAdvice` / `@ControllerAdvice` for consistent error responses
10. **Test structure** — Mirror the main source structure in `src/test/java/`
