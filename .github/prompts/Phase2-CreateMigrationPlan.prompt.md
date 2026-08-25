---
agent: 'ColdFusion to Java Migration'
model: Claude Sonnet 5 (copilot)
tools: [vscode, execute, read, browser, edit, search, web, azure/search]
---

# Phase 2: Detailed File-by-File Migration Plan

## Objective

Create a comprehensive, file-by-file migration plan that documents exactly how each ColdFusion (CFML) file with business logic will be migrated to Java / Spring Boot. This plan ensures the model has complete context when executing migrations.

**Prerequisites**: 
- Phase 0: `Application-Discovery-Report.md` completed
- Phase 1: `Technical-Assessment-Report.md` completed with user preferences

## Why This Phase is Critical

When migrating code, the model needs to understand:
1. **What** each file does (purpose, responsibilities)
2. **How** it interacts with other components (dependencies)
3. **Where** it should go in Java / Spring Boot (target structure)
4. **What patterns** to use (CFML → Java mapping)
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
- Component inventory (handlers/pages, components/models, services, views)
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

### 3.1 Request Handlers / Pages Migration Plan

For each ColdFusion request handler (`.cfc`) or `.cfm` controller page discovered in Phase 0:

```markdown
### Handler / Page: [ControllerName]

| Property | Value |
|----------|-------|
| **Source File** | `handlers/User.cfc` (or `users.cfm`) |
| **Target File** | `src/main/java/com/example/projectname/controller/UserController.java` |
| **Purpose** | Handle user CRUD operations and authentication |
| **HTTP Methods** | GET, POST, PUT, DELETE |

#### Actions/Methods Mapping

| CFML Function / Action | HTTP | Route | Java Method | Notes |
|------------------------|------|-------|-------------|-------|
| `index()` / `url.action=list` | GET | /users | `@GetMapping list()` | Returns view or ResponseEntity |
| `show(id)` | GET | /users/{id} | `@GetMapping("/{id}") show(@PathVariable Long id)` | Validate id exists |
| `save(rc)` (from `form` scope) | POST | /users | `@PostMapping create(@Valid @RequestBody CreateUserDto dto)` | Use DTO + Bean Validation |
| `update(id, rc)` | PUT | /users/{id} | `@PutMapping("/{id}") update(@PathVariable Long id, @Valid @RequestBody UpdateUserDto dto)` | |
| `delete(id)` | DELETE | /users/{id} | `@DeleteMapping("/{id}") delete(@PathVariable Long id)` | Soft delete if applicable |

#### Dependencies
- **Injected Services**: `UserService`, `Logger` (via SLF4J)
- **Used Models**: `User`, `CreateUserDto`, `UpdateUserDto`
- **Security**: `@PreAuthorize` annotation

#### Business Logic in Handler/Page
⚠️ CFML often mixes logic, SQL, and presentation in the same `.cfm` page. Document for extraction:
| Logic | Current Location | Target Location |
|-------|-----------------|-----------------|
| Email validation | `save()` / inline in page | `UserService.validateEmail()` |
| Role assignment | `save()` / inline in page | `UserService.assignDefaultRole()` |
| Inline `<cfquery>` | Within the `.cfm` page | Repository / Service |

#### Migration Complexity: [Low/Medium/High]
#### Estimated Effort: [X hours]
```

### 3.2 Components/Entities Migration Plan

For each ColdFusion component / CF-ORM persistent CFC:

```markdown
### Component: [ModelName]

| Property | Value |
|----------|-------|
| **Source File** | `cfcs/User.cfc` |
| **Target File** | `src/main/java/com/example/projectname/entity/User.java` |
| **Database Table** | `users` |
| **Primary Key** | `id` (auto-increment) |

#### Properties Mapping

| CFML Property (`<cfproperty>`) | CFML Type | Java Property | Java Type | Notes |
|--------------------------------|-----------|---------------|-----------|-------|
| `id` | numeric | `id` | `Long` | Primary key, @Id @GeneratedValue |
| `email` | string | `email` | `String` | Required, unique |
| `password` | string | `passwordHash` | `String` | Hashed |
| `name` | string | `name` | `String` | Max 100 chars |
| `created_at` | date | `createdAt` | `LocalDateTime` | @CreatedDate |
| `updated_at` | date | `updatedAt` | `LocalDateTime` | @LastModifiedDate, nullable |
| `deleted_at` | date | `deletedAt` | `LocalDateTime` | Soft delete, nullable |

#### Relationships

| CFML Relationship (`fieldtype`) | Type | Related Component | JPA Annotation | Notes |
|---------------------------------|------|-------------------|----------------|-------|
| `orders` (`fieldtype="one-to-many"`) | one-to-many | Order | `@OneToMany(mappedBy = "user") List<Order> orders` | |
| `role` (`fieldtype="many-to-one"`) | many-to-one | Role | `@ManyToOne @JoinColumn(name = "role_id") Role role` | FK |
| `profile` (`fieldtype="one-to-one"`) | one-to-one | Profile | `@OneToOne(mappedBy = "user") Profile profile` | |

#### CFC Finder Methods → Spring Data JPA Specifications

| CFML Finder / Query | Purpose | Spring Data JPA Implementation |
|---------------------|---------|-------------------------------|
| `getActiveUsers()` (`<cfquery>` WHERE active=1) | Only active users | JPA Specification or `@Query` method |
| `getAdmins()` (`<cfquery>` WHERE role='admin') | Only admin users | Custom repository method or Specification |

#### CF-ORM Events → JPA Entity Listeners

| CF-ORM Event | Purpose | JPA Implementation |
|--------------|---------|-------------------|
| `preInsert()` | Set defaults | `@PrePersist` callback or `@EntityListeners` |
| `preDelete()` | Cascade soft delete | `@PreRemove` callback or Hibernate `@SQLDelete` |

#### Validation Rules
Document CFML validation to convert (`<cfparam type="...">`, `isValid()`, `<cfproperty validate>`):
| CFML Validation | Java Equivalent |
|-----------------|-----------------|
| `<cfparam name="email" type="string">` / required | `@NotNull` / `@NotBlank` |
| `isValid("email", form.email)` | `@Email` |
| `len(name) LTE 100` | `@Size(max = 100)` |
| Manual `<cfquery>` uniqueness check | Unique constraint + service validation |

#### Migration Complexity: [Low/Medium/High]
#### Estimated Effort: [X hours]
```

### 3.3 Services Migration Plan

For each ColdFusion service CFC (this is where business logic lives):

```markdown
### Service: [ServiceName]

| Property | Value |
|----------|-------|
| **Source File** | `cfcs/PaymentService.cfc` |
| **Target Interface** | `src/main/java/com/example/projectname/service/PaymentService.java` (interface) |
| **Target Implementation** | `src/main/java/com/example/projectname/service/impl/PaymentServiceImpl.java` |
| **Purpose** | Handle all payment processing logic |

#### Dependencies

| CFML Dependency | Wiring Method | Java Equivalent |
|-----------------|---------------|-----------------|
| `gatewayCFC` | `createObject("component",...)` / `init()` | `StripeClient` (from com.stripe:stripe-java) |
| `orderService` | `application` scope singleton | `OrderRepository` (Spring Data JPA) |
| `<cflog>` / `writeLog()` | Built-in logging | `Logger` (SLF4J via @Slf4j) |
| `application.settings` / `settings.ini.cfm` | Config struct | `@ConfigurationProperties PaymentProperties` |

#### Methods Mapping

| CFML Function | Signature | Java Method | Signature | Notes |
|---------------|-----------|-------------|-----------|-------|
| `processPayment` | `(order, struct card): PaymentResult` | `processPayment` | `(Order order, CardDto card): PaymentResult` | Use @Transactional |
| `refund` | `(transactionId, amount): boolean` | `refund` | `(String transactionId, BigDecimal amount): boolean` | Use BigDecimal for money |
| `validateCard` | `(struct card): ValidationResult` | `validateCard` | `(CardDto card): ValidationResult` | |

#### Business Logic Documentation

**⚠️ CRITICAL: Document ALL business rules in this service:**

| Business Rule | CFML Implementation | Line Numbers | Java Implementation Notes |
|---------------|---------------------|--------------|--------------------------|
| Minimum order $10 | `<cfif order.total LT 10><cfthrow ...></cfif>` | L45-47 | Same logic, use Bean Validation |
| Max refund 30 days | `<cfif dateDiff("d", order.created_at, now()) GT 30>` | L78-82 | Use java.time Period/Duration comparison |
| Fraud check | `application.fraudService.check(card)` | L55-60 | Inject FraudService |
| Retry on failure | `<cfloop>` retry up to 3 times around gateway call | L65-70 | Use Spring Retry or Resilience4j |

#### Error Handling

| CFML Error (`<cfthrow type="...">`) | When Thrown | Java Exception |
|-------------------------------------|-------------|----------------|
| `<cfthrow type="PaymentFailed">` | Gateway returns error | `PaymentFailedException` (custom) |
| `<cfthrow type="InsufficientFunds">` | Card declined | `InsufficientFundsException` (custom) |
| `<cfthrow type="Validation">` | Invalid card data | `ValidationException` |

#### External API Calls

| API | Method | Purpose | Java Implementation |
|-----|--------|---------|---------------------|
| Stripe (`<cfhttp>`) | `POST /v1/charges` | Charge card | Use com.stripe:stripe-java SDK |
| Stripe (`<cfhttp>`) | `POST /v1/refunds` | Process refund | Use com.stripe:stripe-java SDK |

#### Migration Complexity: [Low/Medium/High]
#### Estimated Effort: [X hours]
```

### 3.4 Views Migration Plan

For each ColdFusion `.cfm` view / custom tag:

```markdown
### View: [ViewName]

| Property | Value |
|----------|-------|
| **Source File** | `users/index.cfm` |
| **Target File** | `src/main/resources/templates/users/index.html` |
| **Layout** | `header.cfm` / `footer.cfm` → `layout.html` (Thymeleaf layout) |
| **Handler** | `UserController.list()` |

#### View Components Used

| CFML Include / Custom Tag | Purpose | Thymeleaf Equivalent |
|---------------------------|---------|----------------------|
| `<cfinclude template="sidebar.cfm">` | Navigation | `th:insert="~{fragments/sidebar}"` |
| `<cf_alert type="success">` | Alert box | Thymeleaf fragment with parameters |
| `<cfmodule template="card.cfm">` | Card wrapper | Thymeleaf fragment |

#### Data Passed to View

| Variable | CFML Type | Description | Java Model Attribute |
|----------|-----------|-------------|---------------------|
| `users` | query / array | List of users | `List<UserDto> users` (via Model.addAttribute) |
| `currentPage` | numeric | Pagination | `int currentPage` |
| `totalPages` | numeric | Pagination | `int totalPages` |

#### CFML → Thymeleaf Syntax Mapping

| CFML Syntax | Thymeleaf Equivalent |
|-------------|----------------------|
| `<cfoutput>#variable#</cfoutput>` | `th:text="${variable}"` |
| `<cfoutput>#variable#</cfoutput>` (raw HTML) | `th:utext="${html}"` |
| `<cfif condition>` | `th:if="${condition}"` |
| `<cfloop array="#items#" index="item">` | `th:each="item : ${items}"` |
| `<cfinclude template="header.cfm">` (layout) | `layout:decorate="~{layout}"` |
| `<cfsavecontent variable="content">` | `layout:fragment="content"` |
| Content placeholder / include point | `layout:fragment="content"` |
| `<cfinclude template="partial.cfm">` | `th:insert="~{partial}"` |
| `CSRFGenerateToken()` + hidden field | (automatic with Spring Security) |
| `<cfif isUserLoggedIn()>` | `sec:authorize="isAuthenticated()"` (Thymeleaf Spring Security) |

#### JavaScript/CSS Dependencies

| Asset | Source | Target Location |
|-------|--------|-----------------|
| `app.js` | `assets/js/app.js` | `src/main/resources/static/js/app.js` |
| `app.css` | `assets/css/app.css` | `src/main/resources/static/css/app.css` |

#### Migration Complexity: [Low/Medium/High]
#### Estimated Effort: [X hours]
```

### 3.5 Request Lifecycle / Filters Migration Plan

CFML cross-cutting concerns typically live in `Application.cfc` lifecycle methods (or `Application.cfm` for legacy apps), custom tags, and interceptors.

```markdown
### Filter / Lifecycle Hook: [Name]

| Property | Value |
|----------|-------|
| **Source** | `Application.cfc` → `onRequestStart()` (age check) |
| **Target File** | `src/main/java/com/example/projectname/config/CheckAgeInterceptor.java` |
| **Applied To** | Protected routes requiring age verification |

#### CFML Implementation Analysis

```cfml
<cffunction name="onRequestStart" returntype="boolean">
    <cfargument name="targetPage" type="string" required="true">
    <cfif structKeyExists(session, "user") AND session.user.age LT 18>
        <cfthrow type="Forbidden" message="Age requirement not met">
    </cfif>
    <cfreturn true>
</cffunction>
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
- CFML: `Application.cfc` `onRequestStart()` / a custom tag included in request templates
- Java: `WebConfig.java` with `registry.addInterceptor()` or `FilterRegistrationBean`

#### Migration Complexity: [Low/Medium/High]
#### Estimated Effort: [X hours]
```

### 3.6 Background Jobs Migration Plan

```markdown
### Job: [JobName]

| Property | Value |
|----------|-------|
| **Source File** | `tasks/sendWelcomeEmail.cfm` (via `<cfschedule>` or `<cfthread>`) |
| **Target File** | `src/main/java/com/example/projectname/job/SendWelcomeEmailJob.java` |
| **Trigger** | `<cfthread>` / scheduled task after user registration |

#### CFML Implementation

| Property | Value |
|----------|-------|
| Trigger | `<cfschedule>` / CF Administrator scheduled task / `<cfthread>` |
| Interval | On-demand |
| Retries | Manual (`<cftry>` + loop) |
| Timeout | `<cfsetting requesttimeout>` |

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
**Source**: ColdFusion [Engine/Version] / [Framework]
**Target**: Java 21 / Spring Boot 3.x

## Executive Summary

- **Total ColdFusion Files**: [X]
- **Files with Business Logic**: [X]
- **Estimated Total Effort**: [X hours]
- **Migration Waves**: 6

## Target Project Structure

[Full project structure diagram]

## Migration Plan by Component

### Handlers / Pages ([X] files)
[All handler/page migration plans]

### Components/Entities ([X] files)
[All component/model migration plans]

### Services ([X] files)
[All service migration plans]

### Views ([X] files)
[All view migration plans]

### Filters / Lifecycle ([X] files)
[All filter/lifecycle migration plans]

### Background Jobs ([X] files)
[All job migration plans]

## Migration Order

[Complete wave-by-wave migration order]

## Business Logic Preservation Checklist

| Business Rule | Source File | Source Function | Target File | Target Method | Status |
|---------------|-------------|-----------------|-------------|---------------|--------|
| [Rule 1] | [file.cfc] | [function] | [File.java] | [Method] | ⏳ |

## Dependency Migration

| CFML Tag / Java Interop / Module | Maven/Gradle Dependency | Notes |
|----------------------------------|-------------------------|-------|
| [tag/module] | [package] | [notes] |

## Configuration Migration

| CFML Config | Java Config | Notes |
|-------------|-------------|-------|
| `Application.cfc` / `settings.ini.cfm` setting | `application.yml` property | |

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
- Map every CFML pattern to its Java equivalent
- Define clear migration order based on dependencies
- Watch for logic/SQL/presentation mixed in the same `.cfm` page

### Business Logic Priority
- **CRITICAL**: Capture ALL business rules
- Document WHERE logic lives (file, function, line)
- Identify logic in wrong places (`.cfm` pages instead of services)
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
2. ✅ Every handler/page mapped with actions
3. ✅ Every component mapped with properties and relationships
4. ✅ Every service mapped with methods and business logic
5. ✅ Every view mapped with Thymeleaf equivalents
6. ✅ Migration order defined by waves
7. ✅ Business logic preservation checklist
8. ✅ `reports/Report-Status.md` updated

**Next Step**: `/phase3-migratecode` to execute the migration.
