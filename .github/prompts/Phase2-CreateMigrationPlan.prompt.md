---
agent: agent
model: Claude Sonnet 4.5 (copilot)
tools: ['search/codebase', 'search/usages', 'vscode/vscodeAPI', 'read/problems', 'search/changes', 'execute/testFailure', 'vscode/runCommand', 'read/terminalLastCommand', 'vscode/openSimpleBrowser', 'web/fetch', 'search/searchResults', 'web/githubRepo', 'vscode/extensions', 'execute/runTests', 'edit/editFiles', 'search', 'azure-mcp/*']
---

# Phase 2: Detailed File-by-File Migration Plan

## Objective

Create a comprehensive, file-by-file migration plan that documents exactly how each PHP file with business logic will be migrated to Java / Spring Boot. This plan ensures the model has complete context when executing migrations.

**Prerequisites**: 
- Phase 0: `Application-Discovery-Report.md` completed
- Phase 1: `Technical-Assessment-Report.md` completed with user preferences

## Why This Phase is Critical

When migrating code, the model needs to understand:
1. **What** each file does (purpose, responsibilities)
2. **How** it interacts with other components (dependencies)
3. **Where** it should go in Java / Spring Boot (target structure)
4. **What patterns** to use (PHP → Java mapping)
5. **What order** to migrate (dependency chain)

This phase creates that complete context.

---

## Step 1: Review Previous Phases

Read the reports from previous phases:

```
read_file: reports/Application-Discovery-Report.md
read_file: reports/Technical-Assessment-Report.md
```

Extract:
- Component inventory (controllers, models, services, views)
- Business logic locations
- User's target architecture preferences
- Technology mapping decisions

---

## Step 2: Define Java Project Structure

Based on user preferences from Phase 1, define the target Java / Spring Boot project structure:

### For Spring Boot MVC:
```
[ProjectName]/
├── pom.xml (or build.gradle)
├── src/main/java/com/example/projectname/
│   ├── ProjectNameApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── WebConfig.java
│   │   └── ApplicationConfig.java
│   ├── controller/
│   │   ├── HomeController.java
│   │   └── ...
│   ├── service/
│   │   ├── UserService.java
│   │   └── ...
│   ├── repository/
│   │   ├── UserRepository.java
│   │   └── ...
│   ├── entity/
│   │   ├── User.java
│   │   └── ...
│   ├── dto/
│   │   ├── UserDto.java
│   │   └── ...
│   ├── exception/
│   │   └── GlobalExceptionHandler.java
│   └── security/
│       └── CustomUserDetailsService.java
├── src/main/resources/
│   ├── application.yml
│   ├── templates/ (Thymeleaf)
│   │   ├── layout.html
│   │   └── ...
│   ├── static/
│   │   ├── css/
│   │   ├── js/
│   │   └── images/
│   └── db/migration/ (Flyway)
│       └── V1__initial_schema.sql
└── src/test/java/...
```

### For Spring Boot REST API:
```
[ProjectName]/
├── pom.xml (or build.gradle)
├── src/main/java/com/example/projectname/
│   ├── ProjectNameApplication.java
│   ├── config/
│   ├── controller/
│   │   └── UserController.java (@RestController)
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   └── exception/
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/ (Flyway)
└── src/test/java/...
```

### For Vaadin:
```
[ProjectName]/
├── pom.xml (or build.gradle)
├── src/main/java/com/example/projectname/
│   ├── ProjectNameApplication.java
│   ├── views/
│   │   ├── MainLayout.java
│   │   ├── HomeView.java
│   │   └── ...
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── config/
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/ (Flyway)
├── frontend/ (Vaadin frontend resources)
└── src/test/java/...
```

---

## Step 3: Create File-by-File Migration Plan

### 3.1 Controllers Migration Plan

For each PHP controller discovered in Phase 0:

```markdown
### Controller: [ControllerName]

| Property | Value |
|----------|-------|
| **Source File** | `app/Http/Controllers/UserController.php` |
| **Target File** | `src/main/java/com/example/projectname/controller/UserController.java` |
| **Purpose** | Handle user CRUD operations and authentication |
| **HTTP Methods** | GET, POST, PUT, DELETE |

#### Actions/Methods Mapping

| PHP Method | HTTP | Route | Java Method | Notes |
|------------|------|-------|-------------|-------|
| `index()` | GET | /users | `@GetMapping list()` | Returns view or ResponseEntity |
| `show($id)` | GET | /users/{id} | `@GetMapping("/{id}") show(@PathVariable Long id)` | Validate id exists |
| `store(Request $request)` | POST | /users | `@PostMapping create(@Valid @RequestBody CreateUserDto dto)` | Use DTO + Bean Validation |
| `update(Request $request, $id)` | PUT | /users/{id} | `@PutMapping("/{id}") update(@PathVariable Long id, @Valid @RequestBody UpdateUserDto dto)` | |
| `destroy($id)` | DELETE | /users/{id} | `@DeleteMapping("/{id}") delete(@PathVariable Long id)` | Soft delete if applicable |

#### Dependencies
- **Injected Services**: `UserService`, `Logger` (via SLF4J)
- **Used Models**: `User`, `CreateUserDto`, `UpdateUserDto`
- **Security**: `@PreAuthorize` annotation

#### Business Logic in Controller
⚠️ If controller contains business logic, document for extraction:
| Logic | Current Location | Target Location |
|-------|-----------------|-----------------|
| Email validation | `store()` method | `UserService.validateEmail()` |
| Role assignment | `store()` method | `UserService.assignDefaultRole()` |

#### Migration Complexity: [Low/Medium/High]
#### Estimated Effort: [X hours]
```

### 3.2 Models/Entities Migration Plan

For each PHP model:

```markdown
### Model: [ModelName]

| Property | Value |
|----------|-------|
| **Source File** | `app/Models/User.php` |
| **Target File** | `src/main/java/com/example/projectname/entity/User.java` |
| **Database Table** | `users` |
| **Primary Key** | `id` (auto-increment) |

#### Properties Mapping

| PHP Property | PHP Type | Java Property | Java Type | Notes |
|--------------|----------|---------------|-----------|-------|
| `$id` | int | `id` | `Long` | Primary key, @Id @GeneratedValue |
| `$email` | string | `email` | `String` | Required, unique |
| `$password` | string | `passwordHash` | `String` | Hashed |
| `$name` | string | `name` | `String` | Max 100 chars |
| `$created_at` | Carbon | `createdAt` | `LocalDateTime` | @CreatedDate |
| `$updated_at` | Carbon | `updatedAt` | `LocalDateTime` | @LastModifiedDate, nullable |
| `$deleted_at` | Carbon | `deletedAt` | `LocalDateTime` | Soft delete, nullable |

#### Relationships

| PHP Relationship | Type | Related Model | JPA Annotation | Notes |
|------------------|------|---------------|----------------|-------|
| `orders()` | hasMany | Order | `@OneToMany(mappedBy = "user") List<Order> orders` | |
| `role()` | belongsTo | Role | `@ManyToOne @JoinColumn(name = "role_id") Role role` | FK |
| `profile()` | hasOne | Profile | `@OneToOne(mappedBy = "user") Profile profile` | |

#### Eloquent Scopes → Spring Data JPA Specifications

| PHP Scope | Purpose | Spring Data JPA Implementation |
|-----------|---------|-------------------------------|
| `scopeActive($query)` | Only active users | JPA Specification or @Where annotation |
| `scopeAdmins($query)` | Only admin users | Custom repository method or Specification |

#### Model Events → JPA Entity Listeners

| PHP Event | Purpose | JPA Implementation |
|-----------|---------|-------------------|
| `creating` | Set defaults | `@PrePersist` callback or `@EntityListeners` |
| `deleting` | Cascade soft delete | `@PreRemove` callback or Hibernate `@SQLDelete` |

#### Validation Rules
Document Laravel validation rules to convert:
| PHP Rule | Java Equivalent |
|----------|-----------------|
| `required` | `@NotNull` / `@NotBlank` |
| `email` | `@Email` |
| `max:100` | `@Size(max = 100)` |
| `unique:users,email` | Unique constraint + service validation |

#### Migration Complexity: [Low/Medium/High]
#### Estimated Effort: [X hours]
```

### 3.3 Services Migration Plan

For each PHP service (this is where business logic lives):

```markdown
### Service: [ServiceName]

| Property | Value |
|----------|-------|
| **Source File** | `app/Services/PaymentService.php` |
| **Target Interface** | `src/main/java/com/example/projectname/service/PaymentService.java` (interface) |
| **Target Implementation** | `src/main/java/com/example/projectname/service/impl/PaymentServiceImpl.java` |
| **Purpose** | Handle all payment processing logic |

#### Dependencies

| PHP Dependency | Injection Method | Java Equivalent |
|----------------|------------------|-----------------|
| `StripeGateway` | Constructor | `StripeClient` (from com.stripe:stripe-java) |
| `OrderRepository` | Constructor | `OrderRepository` (Spring Data JPA) |
| `Logger` | Facade | `Logger` (SLF4J via @Slf4j) |
| `Config` | Facade | `@ConfigurationProperties PaymentProperties` |

#### Methods Mapping

| PHP Method | Signature | Java Method | Signature | Notes |
|------------|-----------|-------------|-----------|-------|
| `processPayment` | `(Order $order, array $card): PaymentResult` | `processPayment` | `(Order order, CardDto card): PaymentResult` | Use @Transactional |
| `refund` | `(string $transactionId, float $amount): bool` | `refund` | `(String transactionId, BigDecimal amount): boolean` | Use BigDecimal for money |
| `validateCard` | `(array $card): ValidationResult` | `validateCard` | `(CardDto card): ValidationResult` | |

#### Business Logic Documentation

**⚠️ CRITICAL: Document ALL business rules in this service:**

| Business Rule | PHP Implementation | Line Numbers | Java Implementation Notes |
|---------------|-------------------|--------------|--------------------------|
| Minimum order $10 | `if ($order->total < 10) throw...` | L45-47 | Same logic, use Bean Validation |
| Max refund 30 days | `if ($order->created_at->diffInDays(now()) > 30)` | L78-82 | Use java.time Period/Duration comparison |
| Fraud check | `$this->fraudService->check($card)` | L55-60 | Inject FraudService |
| Retry on failure | `retry(3, fn() => $stripe->charge(...))` | L65-70 | Use Spring Retry or Resilience4j |

#### Error Handling

| PHP Exception | When Thrown | Java Exception |
|---------------|-------------|--------------|
| `PaymentFailedException` | Stripe returns error | `PaymentFailedException` (custom) |
| `InsufficientFundsException` | Card declined | `InsufficientFundsException` (custom) |
| `ValidationException` | Invalid card data | `ValidationException` |

#### External API Calls

| API | Method | Purpose | Java Implementation |
|-----|--------|---------|---------------------|
| Stripe | `POST /v1/charges` | Charge card | Use com.stripe:stripe-java SDK |
| Stripe | `POST /v1/refunds` | Process refund | Use com.stripe:stripe-java SDK |

#### Migration Complexity: [Low/Medium/High]
#### Estimated Effort: [X hours]
```

### 3.4 Views Migration Plan

For each PHP view/template:

```markdown
### View: [ViewName]

| Property | Value |
|----------|-------|
| **Source File** | `resources/views/users/index.blade.php` |
| **Target File** | `src/main/resources/templates/users/index.html` |
| **Layout** | `app.blade.php` → `layout.html` (Thymeleaf layout) |
| **Controller** | `UserController.Index()` |

#### View Components Used

| Blade Component | Purpose | Thymeleaf Equivalent |
|-----------------|---------|----------------------|
| `@include('partials.sidebar')` | Navigation | `th:insert="~{fragments/sidebar}"` |
| `<x-alert type="success">` | Alert box | Thymeleaf fragment with parameters |
| `@component('card')` | Card wrapper | Thymeleaf fragment |

#### Data Passed to View

| Variable | PHP Type | Description | Java Model Attribute |
|----------|----------|-------------|---------------------|
| `$users` | Collection | List of users | `List<UserDto> users` (via Model.addAttribute) |
| `$currentPage` | int | Pagination | `int currentPage` |
| `$totalPages` | int | Pagination | `int totalPages` |

#### Blade → Thymeleaf Syntax Mapping

| Blade Syntax | Thymeleaf Equivalent |
|--------------|----------------------|
| `{{ $variable }}` | `th:text="${variable}"` |
| `{!! $html !!}` | `th:utext="${html}"` |
| `@if($condition)` | `th:if="${condition}"` |
| `@foreach($items as $item)` | `th:each="item : ${items}"` |
| `@extends('layout')` | `layout:decorate="~{layout}"` |
| `@section('content')` | `layout:fragment="content"` |
| `@yield('content')` | `layout:fragment="content"` |
| `@include('partial')` | `th:insert="~{partial}"` |
| `@csrf` | (automatic with Spring Security) |
| `@auth` | `sec:authorize="isAuthenticated()"` (Thymeleaf Spring Security) |

#### JavaScript/CSS Dependencies

| Asset | Source | Target Location |
|-------|--------|-----------------|
| `app.js` | `resources/js/app.js` | `src/main/resources/static/js/app.js` |
| `app.css` | `resources/css/app.css` | `src/main/resources/static/css/app.css` |

#### Migration Complexity: [Low/Medium/High]
#### Estimated Effort: [X hours]
```

### 3.5 Middleware Migration Plan

```markdown
### Middleware: [MiddlewareName]

| Property | Value |
|----------|-------|
| **Source File** | `app/Http/Middleware/CheckAge.php` |
| **Target File** | `src/main/java/com/example/projectname/config/CheckAgeInterceptor.java` |
| **Applied To** | Routes with `age.check` middleware |

#### PHP Implementation Analysis

```php
public function handle($request, Closure $next, $minAge = 18)
{
    if ($request->user()->age < $minAge) {
        abort(403, 'Age requirement not met');
    }
    return $next($request);
}
```

#### Java Implementation Plan

```java
@Component
public class CheckAgeInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Implementation here
        return true; // continue processing
    }
}
```

#### Registration
- PHP: `Kernel.php` in `$routeMiddleware`
- Java: `WebConfig.java` with `registry.addInterceptor()` or `FilterRegistrationBean`

#### Migration Complexity: [Low/Medium/High]
#### Estimated Effort: [X hours]
```

### 3.6 Background Jobs Migration Plan

```markdown
### Job: [JobName]

| Property | Value |
|----------|-------|
| **Source File** | `app/Jobs/SendWelcomeEmail.php` |
| **Target File** | `src/main/java/com/example/projectname/job/SendWelcomeEmailJob.java` |
| **Trigger** | Queue dispatch after user registration |

#### PHP Implementation

| Property | Value |
|----------|-------|
| Queue | `emails` |
| Delay | None |
| Retries | 3 |
| Timeout | 60 seconds |

#### Java Implementation Plan

**Option A: Spring @Async + Azure Service Bus**
```java
@Service
public class EmailQueueProcessor {
    @Async
    public void processWelcomeEmail(Long userId) {
        // Process messages from Azure Service Bus
    }
}
```

**Option B: Spring Scheduler (if not using Azure)**
```java
@Scheduled(fixedRate = 60000)
public void processEmailQueue() {
    emailService.sendPendingEmails();
}
```

#### Migration Complexity: [Low/Medium/High]
#### Estimated Effort: [X hours]
```

---

## Step 4: Define Migration Order

**CRITICAL**: Define the order files should be migrated based on dependencies.

```markdown
## Migration Order

### Wave 1: Foundation (No dependencies)
| Order | Component | File | Depends On |
|-------|-----------|------|------------|
| 1.1 | Project Setup | Create pom.xml, Application.java | - |
| 1.2 | Configuration | application.yml | - |
| 1.3 | Base Entities | User.java, BaseEntity.java | - |
| 1.4 | JPA Config | JpaConfig.java, repositories | Entities |

### Wave 2: Data Layer
| Order | Component | File | Depends On |
|-------|-----------|------|------------|
| 2.1 | Entities | All @Entity classes | Wave 1 |
| 2.2 | DTOs | All DTO / record classes | Entities |
| 2.3 | Repositories | Spring Data JPA repository interfaces | JPA Config |

### Wave 3: Business Logic
| Order | Component | File | Depends On |
|-------|-----------|------|------------|
| 3.1 | Service Interfaces | UserService.java (interface), etc. | DTOs |
| 3.2 | Service Implementations | UserServiceImpl.java, etc. | Interfaces, Repositories |
| 3.3 | Validators | Bean Validation annotations + custom validators | DTOs |

### Wave 4: Controllers
| Order | Component | File | Depends On |
|-------|-----------|------|------------|
| 4.1 | Base Controller | BaseController.java | - |
| 4.2 | All Controllers | UserController.java, etc. | Services, DTOs |

### Wave 5: UI Layer
| Order | Component | File | Depends On |
|-------|-----------|------|------------|
| 5.1 | Layout | layout.html (Thymeleaf) | - |
| 5.2 | Fragments | All Thymeleaf fragments | Layout |
| 5.3 | Views | All Thymeleaf templates | Fragments, DTOs |
| 5.4 | Static Assets | CSS, JS, images | - |

### Wave 6: Infrastructure
| Order | Component | File | Depends On |
|-------|-----------|------|------------|
| 6.1 | Filters/Interceptors | All filters and interceptors | - |
| 6.2 | Background Jobs | All scheduled/async jobs | Services |
| 6.3 | Configuration | @Configuration and @Bean definitions | All services |
```

---

## Step 5: Generate Migration Plan Document

Create `reports/Migration-Plan-Detailed.md`:

```markdown
# Detailed Migration Plan

**Application**: [Name]
**Generated**: [Date/Time]
**Source**: PHP [Version] / [Framework]
**Target**: Java 21 / Spring Boot 3.x

## Executive Summary

- **Total PHP Files**: [X]
- **Files with Business Logic**: [X]
- **Estimated Total Effort**: [X hours]
- **Migration Waves**: 6

## Target Project Structure

[Full project structure diagram]

## Migration Plan by Component

### Controllers ([X] files)
[All controller migration plans]

### Models/Entities ([X] files)
[All model migration plans]

### Services ([X] files)
[All service migration plans]

### Views ([X] files)
[All view migration plans]

### Middleware ([X] files)
[All middleware migration plans]

### Background Jobs ([X] files)
[All job migration plans]

## Migration Order

[Complete wave-by-wave migration order]

## Business Logic Preservation Checklist

| Business Rule | Source File | Source Method | Target File | Target Method | Status |
|---------------|-------------|---------------|-------------|---------------|--------|
| [Rule 1] | [file.php] | [method] | [File.cs] | [Method] | ⏳ |

## Dependency Migration

| Composer Package | Maven/Gradle Dependency | Notes |
|------------------|-------------------------|-------|
| [package] | [package] | [notes] |

## Configuration Migration

| PHP Config | Java Config | Notes |
|------------|-------------|-------|
| `.env` key | `application.yml` property | |

## Next Steps

Proceed to code migration:

**Run**: `/phase3-migratecode`

The migration will follow the wave order defined in this plan.
```

---

## Step 6: Update Status Report

Update `reports/Report-Status.md`:

```markdown
## Phase 2 Summary

- **Migration Plan**: Complete
- **Files Documented**: [X]
- **Business Rules Mapped**: [X]
- **Estimated Effort**: [X] hours

## Next Step

Run `/phase3-migratecode` to execute the migration following this plan.
```

---

## Rules & Constraints

### Documentation Depth
- Document EVERY file with business logic
- Include exact line numbers for business rules
- Map every PHP pattern to its Java equivalent
- Define clear migration order based on dependencies

### Business Logic Priority
- **CRITICAL**: Capture ALL business rules
- Document WHERE logic lives (file, method, line)
- Identify logic in wrong places (controllers instead of services)
- Plan for refactoring if needed

### File Reading
- Read **2000 lines at a time** for context
- Use `semantic_search` to find related code
- Use `grep_search` for specific patterns

### Do NOT
- Do NOT start writing Java code yet
- Do NOT create the Java project yet
- Focus ONLY on creating the detailed plan

---

## Deliverables

At the end of Phase 2, you should have:

1. ✅ `reports/Migration-Plan-Detailed.md` - Complete file-by-file plan
2. ✅ Every controller mapped with actions
3. ✅ Every model mapped with properties and relationships
4. ✅ Every service mapped with methods and business logic
5. ✅ Every view mapped with Thymeleaf equivalents
6. ✅ Migration order defined by waves
7. ✅ Business logic preservation checklist
8. ✅ `reports/Report-Status.md` updated

**Next Step**: `/phase3-migratecode` to execute the migration.
