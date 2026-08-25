---
name: php-to-java-mapping
description: PHP to Java 21 / Spring Boot 3.x code mapping reference. Use when converting PHP code patterns to Java equivalents including framework mapping, authentication, templates (Blade to Thymeleaf), packages (Composer to Maven/Gradle), and validation rules.
---

# PHP to Java 21 / Spring Boot 3 Mapping Reference

> Use this skill when migrating PHP code to Java 21 with Spring Boot 3.x. It provides direct mappings for common patterns.

## Framework Mapping

| PHP Framework | Java Equivalent |
|---------------|-----------------|
| Laravel | Spring Boot MVC |
| Symfony | Spring Framework (Spring MVC) |
| CodeIgniter | Spring Boot (lightweight REST) |
| Slim | Spring Boot REST Controllers |
| Lumen | Spring Boot REST Controllers |

## Architecture Pattern Mapping

| PHP Pattern | Java Equivalent |
|-------------|-----------------|
| MVC (Laravel/Symfony) | Spring MVC (`@Controller` + Thymeleaf) |
| API-only (Slim/Lumen) | Spring Boot REST (`@RestController`) |
| Blade/Twig templates | Thymeleaf templates |
| LiveWire | Vaadin (or SPA frontend with Spring REST backend) |

## Data Access Mapping

| PHP | Java |
|-----|------|
| Eloquent ORM | JPA / Hibernate (via Spring Data JPA) |
| Doctrine ORM | JPA / Hibernate |
| PDO | JDBC / Spring JdbcTemplate |
| Query Builder | JPQL / Criteria API |
| Raw SQL | Native `@Query` or JdbcTemplate |
| Migrations (Artisan) | Flyway or Liquibase |

## Authentication Mapping

| PHP | Java |
|-----|------|
| Laravel Auth | Spring Security (form login) |
| Laravel Sanctum | JWT via jjwt / java-jwt |
| Laravel Passport | Spring Security OAuth2 Resource Server |
| PHP Sessions | Spring Session (HttpSession) |
| tymon/jwt-auth | `io.jsonwebtoken:jjwt-api` |
| Socialite | Spring Security OAuth2 Client |
| Entra ID (Azure AD) | Spring Security + MSAL4J / `spring-cloud-azure-starter-active-directory` |

## Dependency Injection

| PHP | Java |
|-----|------|
| Laravel Container | Spring ApplicationContext (built-in IoC) |
| Symfony DI | Spring ApplicationContext |
| Service Providers | `@Configuration` classes with `@Bean` methods |
| Facades | Injected `@Service` / `@Component` beans |
| `app()->make(SomeClass::class)` | `@Autowired` or constructor injection |

## Template Syntax Mapping (Blade → Thymeleaf)

| Blade (PHP) | Thymeleaf (Java) |
|-------------|-------------------|
| `{{ $var }}` | `th:text="${var}"` |
| `{!! $html !!}` | `th:utext="${html}"` |
| `@if($cond)` | `th:if="${cond}"` |
| `@unless($cond)` | `th:unless="${cond}"` |
| `@foreach($items as $item)` | `th:each="item : ${items}"` |
| `@for($i=0; $i<10; $i++)` | `th:each="i : ${#numbers.sequence(0,9)}"` |
| `@extends('layout')` | `layout:decorate="~{layout}"` (Thymeleaf Layout Dialect) |
| `@section('content')` | `layout:fragment="content"` |
| `@yield('content')` | `layout:fragment="content"` (in layout) |
| `@include('partial')` | `th:insert="~{fragments/partial}"` |
| `@csrf` | Automatic with Thymeleaf + Spring Security (`th:action`) |
| `@auth` | `sec:authorize="isAuthenticated()"` (Thymeleaf Spring Security) |
| `{{ route('name') }}` | `th:href="@{/path}"` or `@{/controller/action}` |
| `@component` | Thymeleaf fragment with parameters |
| `{{ $loop->index }}` | `${iterStat.index}` (with `th:each="item, iterStat"`) |
| `@empty` | `th:if="${#lists.isEmpty(items)}"` |
| `@switch / @case` | `th:switch="${var}"` / `th:case="'value'"` |

## Package Mapping (Composer → Maven/Gradle)

| Composer Package | Maven/Gradle Dependency |
|-----------------|--------------------------|
| `guzzlehttp/guzzle` | `java.net.http.HttpClient` (built-in JDK 11+) or `org.springframework.boot:spring-boot-starter-webflux` (WebClient) |
| `stripe/stripe-php` | `com.stripe:stripe-java` |
| `intervention/image` | `net.coobird:thumbnailator` or `org.imgscalr:imgscalr-lib` |
| `tymon/jwt-auth` | `io.jsonwebtoken:jjwt-api` + `jjwt-impl` + `jjwt-jackson` |
| `predis/predis` | `org.springframework.boot:spring-boot-starter-data-redis` |
| `aws/aws-sdk-php` | `software.amazon.awssdk:bom` (AWS SDK v2) |
| `league/flysystem` | `software.amazon.awssdk:s3` or `com.azure:azure-storage-blob` |
| `phpmailer/phpmailer` | `org.springframework.boot:spring-boot-starter-mail` (JavaMailSender) |
| `monolog/monolog` | SLF4J + Logback (built-in with Spring Boot) |
| `nesbot/carbon` | `java.time.LocalDateTime` / `ZonedDateTime` (built-in JDK 8+) |
| `league/csv` | Apache Commons CSV (`org.apache.commons:commons-csv`) |
| `spatie/laravel-permission` | Spring Security roles/authorities |
| `laravel/telescope` | Spring Boot Actuator + Micrometer |
| `barryvdh/laravel-debugbar` | Spring Boot DevTools |

## Configuration Mapping

| PHP | Java |
|-----|------|
| `.env` files | `application.yml` / `application.properties` + Environment Variables |
| `config/*.php` | `@ConfigurationProperties` classes |
| `env('KEY')` | `@Value("${key}")` or `Environment.getProperty("key")` |
| `config('app.name')` | `@Value("${app.name}")` or injected `@ConfigurationProperties` bean |
| `config/database.php` | `spring.datasource.*` in `application.yml` |
| `config/cache.php` | `spring.cache.*` in `application.yml` |
| `config/mail.php` | `spring.mail.*` in `application.yml` |

## Background Jobs Mapping

| PHP | Java |
|-----|------|
| Laravel Queues (database driver) | `@Async` + `ThreadPoolTaskExecutor` |
| Laravel Queues (Redis/SQS) | Spring AMQP (RabbitMQ) or Spring Kafka |
| Symfony Messenger | Spring AMQP or Spring Integration |
| Laravel Scheduler | `@Scheduled` with cron expressions |
| Artisan commands | Spring Boot CLI (`CommandLineRunner` / `ApplicationRunner`) |
| Laravel Horizon | Spring Boot Actuator + custom queue monitoring |
| Job chaining | Spring Batch |

## Validation Mapping

| Laravel Validation | Java (Jakarta Bean Validation) |
|-------------------|-------------------------------|
| `required` | `@NotNull` / `@NotBlank` (for strings) |
| `email` | `@Email` |
| `max:100` | `@Size(max = 100)` (string) / `@Max(100)` (number) |
| `min:1` | `@Size(min = 1)` (string) / `@Min(1)` (number) |
| `between:1,100` | `@Size(min = 1, max = 100)` or `@Range(min = 1, max = 100)` (Hibernate) |
| `numeric` | Use numeric type (`Integer`, `Long`, `BigDecimal`) |
| `string` | Use `String` type |
| `unique:table` | Custom validator with repository check |
| `confirmed` | Custom `@FieldMatch` validator |
| `regex:/pattern/` | `@Pattern(regexp = "pattern")` |
| `date` | `@PastOrPresent` / `@FutureOrPresent` with `LocalDate` type |
| `in:a,b,c` | Use Java `enum` type |
| `nullable` | Omit `@NotNull` (fields are nullable by default in Java) |
| `url` | `@URL` (Hibernate Validator) |
| Form Requests | `@Valid` on DTO parameter + validation annotations on DTO fields |

## Code Examples

See the [examples](./examples/) directory for sample conversions:
- [Controller example](./examples/controller-example.java) - PHP controller to Spring Boot controller
- [Service example](./examples/service-example.java) - PHP service to Spring Boot service
- [Model example](./examples/model-example.java) - Eloquent model to JPA entity
