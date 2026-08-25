---
name: spring-boot-project-structure
description: Spring Boot 3.x project structure and scaffolding templates for migrated ColdFusion applications. Use when creating new Spring Boot projects, setting up folder structure, or generating boilerplate code for Spring MVC or REST APIs.
---

# Spring Boot 3 Project Structure Guide

> Use this skill when setting up the Spring Boot 3.x project structure for migrated ColdFusion (CFML)
> applications.

## Recommended Project Structure

### Spring Boot MVC Project (from ColdFusion `.cfm` + CFC apps)

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
│   │   │   ├── entity/                      # JPA entities (from CF-ORM / CFC models)
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
│   │   │   └── util/                        # Utility classes (from includes/udf.cfm)
│   │   │       └── SlugUtil.java
│   │   └── resources/
│   │       ├── application.yml              # Main configuration
│   │       ├── application-dev.yml          # Dev profile overrides
│   │       ├── application-prod.yml         # Prod profile overrides
│   │       ├── templates/                   # Thymeleaf templates (from .cfm views / custom tags)
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

### Spring Boot REST API Project (from Taffy / ColdBox REST APIs)

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

## ColdFusion to Spring Boot Folder Mapping

> CFML layouts vary by engine and framework. This maps typical **vanilla / legacy `Application.cfm`-style**
> apps (like a `.cfm` + `cfcs/` layout) and notes **ColdBox** equivalents where relevant.

| ColdFusion (CFML) | Spring Boot (Java) |
|-------------------|--------------------|
| `.cfm` controller pages / `handlers/` (ColdBox) | `controller/` |
| `api/`, `mobile/` endpoints / Taffy resources | `controller/api/` or `controller/` with `@RestController` |
| `cfcs/` persistent model CFCs (CF-ORM) | `entity/` |
| `cfcs/` service CFCs (application-scope singletons) | `service/` |
| DataMgr / DAO CFCs / `<cfquery>` gateways | `repository/` (Spring Data JPA interfaces) |
| Form/URL argument structs / request beans | `dto/` (request records with validation annotations) |
| API response structs / `serializeJSON()` output | `dto/` (response records) |
| `onError()` / custom error `.cfm` templates | `exception/` |
| `Application.cfc` `onRequestStart` / custom-tag filters | `config/` (Filters/Interceptors) or `security/` |
| `Application.cfc` / bootstrap CFCs / ColdBox `config/` | `config/` (`@Configuration` classes) |
| `.cfm` views / `views/` directory | `resources/templates/` (Thymeleaf) |
| Layout `.cfm` (via `<cfinclude>` / `onRequest` wrap) | `resources/templates/layout/` |
| Custom tags (`tags/`, `<cf_x>`, `<cfmodule>`) | `resources/templates/fragments/` |
| Web-root static assets (`css/`, `js/`, `images/`) | `resources/static/` |
| URL routing via `.cfm` file paths / SES / `onRequestStart` | `@Controller` class annotations (`@GetMapping`, etc.) |
| REST routes (Taffy/ColdBox resources) | `@RestController` class annotations |
| `config/settings.ini.cfm` / CF Administrator | `application.yml` + `@ConfigurationProperties` classes |
| Schema `.sql` scripts / DataMgr auto-schema | `resources/db/migration/` (Flyway SQL) |
| Seed CFCs / `onApplicationStart` bootstrap inserts | `CommandLineRunner` beans or Flyway callbacks |
| File-storage directories on disk | External storage (S3, Azure Blob) or `/var/data/` |
| `Application.cfc this.*` / environment config | `application.yml` + environment variables |
| `box.json` (CommandBox) / `/lib` JARs / CF mappings | `pom.xml` (Maven) or `build.gradle` (Gradle) |
| `/tests` (TestBox / MXUnit) | `src/test/java/` |

## Java Naming Conventions

| Concept | ColdFusion (CFML) | Java |
|---------|-------------------|------|
| Files | `page.cfm` / `PascalCase.cfc` | `PascalCase.java` |
| Classes / Components | `PascalCase` CFC | `PascalCase` |
| Methods | `camelCase` `<cffunction>` | `camelCase` |
| Variables | `camelCase` (scoped: `local.x`, `arguments.x`) | `camelCase` |
| Properties / Fields | `camelCase` `cfproperty` / `variables` scope | `camelCase` |
| Constants | `UPPER_SNAKE_CASE` (application scope) | `UPPER_SNAKE_CASE` (static final) |
| Packages | Dotted CFC paths (`cfcs.model.User`) | `lowercase.dot.separated` |
| Interfaces | CFC `interface` / duck typing | No prefix/suffix convention (e.g., `ProductService`) |
| Enums | Strings / lists | `PascalCase` (values: `UPPER_SNAKE_CASE`) |
| DTOs | Structs | Java `record` types (Java 17+) |
| Test classes | `*Test.cfc` (TestBox/MXUnit) | `PascalCaseTest` |

## Key Differences from ColdFusion Structure

| ColdFusion Convention | Java / Spring Convention |
|-----------------------|--------------------------|
| One `.cfm` file = controller **and** view combined | Separate `@Controller` method + Thymeleaf template |
| Typeless variables (`<cfset x = ...>`) | Explicit, static types |
| CFCs created on demand / stored in `application` scope | Spring-managed beans in the IoC container |
| `settings.ini.cfm` / CF Administrator settings | `application.yml` or `application.properties` |
| `Application.cfc` / `Application.cfm` bootstrap | `Application.java` with a `main()` method |
| `box.json` / JARs dropped in `/lib` | `pom.xml` (Maven) or `build.gradle` (Gradle) |
| Shared `application` / `session` scope state | Singleton beans (keep stateless) + `HttpSession` |
| `variables` / `this` scope inside a CFC | Instance fields on the class |
| `createObject("component", ...)` / `new` | Constructor injection / `@Autowired` |

## Best Practices

1. **One class per file** — Each `.java` file contains one public class (Java requirement)
2. **Constructor injection** — Prefer constructor injection over `@Autowired` field injection
3. **Interface-based services** — Optional but recommended for testability; Spring proxies work without interfaces too
4. **DTOs as records** — Use Java `record` types for request/response DTOs (immutable, concise)
5. **@ConfigurationProperties** — Use for typed configuration instead of scattered `@Value` annotations
6. **Profile-specific config** — Use `application-{profile}.yml` for environment differences
7. **Package by feature** — For large projects, consider packaging by feature instead of by layer
8. **Async everywhere** — Use `@Async` for non-blocking operations (replaces `<cfthread>`), `CompletableFuture` for async returns
9. **Global exception handling** — Use `@RestControllerAdvice` / `@ControllerAdvice` for consistent error responses (replaces `onError()`)
10. **Test structure** — Mirror the main source structure in `src/test/java/`
