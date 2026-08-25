---
agent: agent
model: Claude Sonnet 4.5 (copilot)
tools: ['search/codebase', 'search/usages', 'vscode/vscodeAPI', 'read/problems', 'search/changes', 'execute/testFailure', 'vscode/runCommand', 'read/terminalLastCommand', 'vscode/openSimpleBrowser', 'web/fetch', 'search/searchResults', 'web/githubRepo', 'vscode/extensions', 'execute/runTests', 'edit/editFiles', 'search', 'azure-mcp/*']
---

# Phase 3: Execute PHP to Java Code Migration

## Objective

Execute the migration from PHP to Java 21 / Spring Boot 3.x following the detailed file-by-file plan created in Phase 2. This phase creates the actual Java project and migrates all code.

**Prerequisites**:
- Phase 0: `Application-Discovery-Report.md` completed
- Phase 1: `Technical-Assessment-Report.md` completed
- Phase 2: `Migration-Plan-Detailed.md` completed

---

## Step 1: Review Migration Plan

Before starting, read the migration plan:

```
read_file: reports/Migration-Plan-Detailed.md
read_file: reports/Technical-Assessment-Report.md
```

Confirm you understand:
- [ ] Target Java architecture (Spring MVC / REST API / Vaadin)
- [ ] Migration wave order
- [ ] All file mappings
- [ ] Business logic locations
- [ ] Package mappings

---

## Step 2: Create Java / Spring Boot Project Structure

### 2.1 Create Project Folder

Create a new folder for the Java project:

```bash
# Do NOT create a new workspace, create a folder in existing workspace
mkdir [ProjectName]-java
cd [ProjectName]-java
```

### 2.2 Initialize Spring Boot Project

Based on the architecture chosen in Phase 1:

**For Spring Boot MVC (web app with Thymeleaf views):**
```bash
# Using Spring Initializr CLI (https://start.spring.io)
spring init --dependencies=web,thymeleaf,data-jpa,validation,security,actuator \
  --java-version=21 --build=maven --packaging=jar \
  --group-id=com.[projectname] --artifact-id=[projectname]-web \
  --name=[ProjectName] [projectname]-web
```

**For Spring Boot REST API:**
```bash
spring init --dependencies=web,data-jpa,validation,security,actuator \
  --java-version=21 --build=maven --packaging=jar \
  --group-id=com.[projectname] --artifact-id=[projectname]-api \
  --name=[ProjectName] [projectname]-api
```

**Alternatively, use start.spring.io:**
- Go to https://start.spring.io
- Select: Maven, Java 21, Spring Boot 3.x
- Add dependencies: Spring Web, Spring Data JPA, Thymeleaf, Spring Security, Validation, Spring Boot Actuator
- Generate and extract into the project folder

### 2.3 Add Required Maven Dependencies

Based on the package mapping from Phase 2, add to `pom.xml`:

```xml
<dependencies>
    <!-- Spring Boot Starters (most added by Initializr) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>

    <!-- Database driver (choose based on Phase 2) -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    <!-- OR for PostgreSQL -->
    <!-- <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency> -->

    <!-- Security (if using Spring Security) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- Security (if using Entra ID) -->
    <dependency>
        <groupId>com.azure.spring</groupId>
        <artifactId>spring-cloud-azure-starter-active-directory</artifactId>
    </dependency>

    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Database migrations -->
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>

    <!-- MapStruct for DTO mapping -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>

    <!-- Lombok (optional, reduces boilerplate) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- Add dependencies mapped from Composer packages -->
    <!-- [Add based on Phase 2 package mapping] -->
</dependencies>
```

### 2.4 Build to Verify Setup

```bash
mvn compile
```

Use `get_errors` to check for any issues.

---

## Step 3: Execute Migration by Waves

**CRITICAL**: Follow the wave order from Phase 2's Migration Plan.

### Wave 1: Foundation

#### 1.1 Configure application.yml

Migrate from PHP `.env` and `config/*.php`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dbname
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
  flyway:
    enabled: true
    locations: classpath:db/migration

# Application settings (migrate from PHP config)
app:
  name: ${APP_NAME:MyApp}

logging:
  level:
    root: INFO
    com.projectname: DEBUG

server:
  port: 8080
```

#### 1.2 Configure Application Main Class and Configuration

Set up the Spring Boot application entry point and configuration beans:

```java
package com.projectname;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

Create a `SecurityConfig` class for the security filter chain:

```java
package com.projectname.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/css/**", "/js/**", "/images/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
            )
            .logout(logout -> logout.permitAll());
        return http.build();
    }
}
```

Create a `WebConfig` class for interceptors and other web configuration:

```java
package com.projectname.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Register interceptors (replaces middleware pipeline)
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // registry.addInterceptor(new RequestLoggingInterceptor());
    }
}
```

---

### Wave 2: Data Layer

#### 2.1 Create JPA Entity Classes

For each PHP model documented in Phase 2:

**Read the PHP model:**
```
read_file: [PHP source path from Migration Plan]
```

**Create the Java entity following the mapping:**

```java
// Example: User entity migrated from app/Models/User.php
package com.projectname.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @NotNull
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(length = 100)
    private String name;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;  // Soft delete

    // Relationships (from Eloquent relationships)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    // JPA lifecycle callbacks (replaces Eloquent events)
    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(OffsetDateTime deletedAt) { this.deletedAt = deletedAt; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }
}
```

#### 2.2 Create Spring Data JPA Repositories

```java
package com.projectname.repository;

import com.projectname.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Soft delete: find only non-deleted users (replaces Eloquent SoftDeletes)
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    List<User> findAllActive();

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findActiveById(Long id);
}
```

#### 2.3 Create DTOs

For each entity, create corresponding DTOs using Java records:

```java
package com.projectname.dto;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

public record CreateUserDto(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String password,
    @NotBlank String name,
    Long roleId
) {}

public record UpdateUserDto(
    @Email String email,
    String name,
    Long roleId
) {}

public record UserDto(
    Long id,
    String email,
    String name,
    String roleName,
    OffsetDateTime createdAt
) {}
```

#### 2.4 Create Flyway Migration Scripts

Create SQL migration files in `src/main/resources/db/migration/`:

```sql
-- V1__initial_schema.sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    role_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

**Build and validate:**
```bash
mvn compile
```

Use `get_errors` to check for issues.

---

### Wave 3: Business Logic (Services)

**⚠️ CRITICAL**: This is where business logic lives. Follow the Phase 2 plan exactly.

#### 3.1 Create Service Interfaces

```java
package com.projectname.service;

import com.projectname.dto.*;
import java.util.List;
import java.util.Optional;

public interface UserService {

    List<UserDto> getAll();
    Optional<UserDto> getById(Long id);
    UserDto create(CreateUserDto dto);
    UserDto update(Long id, UpdateUserDto dto);
    void delete(Long id);

    // Business logic methods from PHP service
    boolean validateEmail(String email);
    void assignDefaultRole(Long userId);
}
```

#### 3.2 Implement Services

For each PHP service in the Migration Plan:

1. **Read the PHP service:**
```
read_file: [PHP service path - read 2000 lines at a time]
```

2. **Migrate each method following the plan:**

```java
package com.projectname.service.impl;

import com.projectname.dto.*;
import com.projectname.model.User;
import com.projectname.repository.UserRepository;
import com.projectname.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Migrated from PHP: UserService::getAll()
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAll() {
        return userRepository.findAllActive().stream()
            .map(u -> new UserDto(
                u.getId(),
                u.getEmail(),
                u.getName(),
                u.getRole().getName(),
                u.getCreatedAt()
            ))
            .toList();
    }

    // Migrated from PHP: UserService::create($data)
    // Business logic preserved from lines XX-YY
    @Override
    public UserDto create(CreateUserDto dto) {
        // Business rule: Validate email uniqueness (from PHP line XX)
        if (!validateEmail(dto.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        var user = new User();
        user.setEmail(dto.email());
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        user.setName(dto.name());

        // Business rule: Assign default role if not specified (from PHP line XX)
        if (dto.roleId() == null || dto.roleId() == 0) {
            assignDefaultRole(user.getId());
        }

        userRepository.save(user);

        return new UserDto(user.getId(), user.getEmail(), user.getName(),
            user.getRole().getName(), user.getCreatedAt());
    }

    // Additional business logic methods...
}
```

#### 3.3 Register Services

In Spring Boot, services are auto-discovered via `@Service` annotation and classpath scanning.
No manual registration needed (unlike `Program.cs` in .NET). Ensure all service classes
are in packages scanned by `@SpringBootApplication`.

For custom beans, create a `@Configuration` class:

```java
package com.projectname.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Add additional @Bean definitions as needed
}
```

**Build and validate:**
```bash
mvn compile
```

---

### Wave 4: Controllers

For each PHP controller in the Migration Plan:

#### 4.1 Read PHP Controller

```
read_file: [PHP controller path from Migration Plan]
```

#### 4.2 Create Spring Controller

```java
package com.projectname.controller;

import com.projectname.dto.*;
import com.projectname.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UsersController {

    private static final Logger log = LoggerFactory.getLogger(UsersController.class);

    private final UserService userService;

    public UsersController(UserService userService) {
        this.userService = userService;
    }

    // Migrated from: UserController::index()
    // Route: GET /users
    @GetMapping
    public String index(Model model) {
        var users = userService.getAll();
        model.addAttribute("users", users);
        return "users/index";
    }

    // Migrated from: UserController::show($id)
    // Route: GET /users/{id}
    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        var user = userService.getById(id);
        if (user.isEmpty()) {
            return "error/404";
        }
        model.addAttribute("user", user.get());
        return "users/details";
    }

    // Migrated from: UserController::store(Request $request)
    // Route: POST /users
    @PostMapping
    public String create(@Valid @ModelAttribute CreateUserDto dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "users/create";
        }

        try {
            var user = userService.create(dto);
            redirectAttributes.addFlashAttribute("success", "User created successfully");
            return "redirect:/users/" + user.id();
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("email", "error.email", ex.getMessage());
            return "users/create";
        }
    }

    // ... additional actions following Phase 2 mapping
}
```

**For REST API controllers:**

```java
package com.projectname.controller.api;

import com.projectname.dto.*;
import com.projectname.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private final UserService userService;

    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable Long id) {
        return userService.getById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody CreateUserDto dto) {
        var user = userService.create(dto);
        return ResponseEntity.created(URI.create("/api/users/" + user.id())).body(user);
    }
}
```

**Build and validate:**
```bash
mvn compile
```

---

### Wave 5: UI Layer (Views)

#### 5.1 Create Layout

Migrate from `resources/views/layouts/app.blade.php`:

**Read PHP layout:**
```
read_file: resources/views/layouts/app.blade.php
```

**Create `src/main/resources/templates/layout/default.html`:**

```html
<!DOCTYPE html>
<html lang="en"
      xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title th:text="${pageTitle} + ' - ' + @{${@environment.getProperty('app.name')}}">App</title>
    <link rel="stylesheet" th:href="@{/css/app.css}" />
</head>
<body>
    <header>
        <!-- Migrate navigation from Blade -->
        <div th:insert="~{fragments/navigation :: nav}"></div>
    </header>

    <main class="container">
        <div layout:fragment="content"></div>
    </main>

    <footer>
        <!-- Footer content -->
    </footer>

    <script th:src="@{/js/app.js}"></script>
    <div layout:fragment="scripts"></div>
</body>
</html>
```

#### 5.2 Migrate Views

For each Blade view in the Migration Plan:

**Read PHP view:**
```
read_file: [Blade template path]
```

**Apply Blade → Thymeleaf conversion:**

| Blade | Thymeleaf |
|-------|-----------|
| `{{ $var }}` | `th:text="${var}"` |
| `{!! $html !!}` | `th:utext="${html}"` |
| `@if($cond)` | `th:if="${cond}"` |
| `@foreach($items as $item)` | `th:each="item : ${items}"` |
| `@extends('layout')` | `layout:decorate="~{layout/default}"` |
| `@section('content')` | `layout:fragment="content"` |
| `@include('partial')` | `th:insert="~{fragments/partial}"` |
| `@csrf` | Automatic with Spring Security + Thymeleaf |
| `@auth` | `sec:authorize="isAuthenticated()"` |
| `{{ route('name') }}` | `th:href="@{/controller/action}"` |
| `{{ asset('path') }}` | `th:src="@{/path}"` |
| `@yield('content')` | `layout:fragment="content"` |
| `@component('alert')` | `th:insert="~{fragments/alert}"` |

#### 5.3 Copy Static Assets

```bash
# Copy CSS to Spring Boot static resources
cp -r resources/css/* src/main/resources/static/css/

# Copy JS
cp -r resources/js/* src/main/resources/static/js/

# Copy images
cp -r public/images/* src/main/resources/static/images/
```

---

### Wave 6: Infrastructure

#### 6.1 Filters and Interceptors

For each PHP middleware in the Migration Plan:

```java
package com.projectname.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain)
            throws ServletException, IOException {

        log.info("Request: {} {}", request.getMethod(), request.getRequestURI());

        filterChain.doFilter(request, response);
    }
}
```

#### 6.2 Scheduled Tasks and Background Services

For each PHP job in the Migration Plan:

```java
package com.projectname.scheduler;

import com.projectname.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class EmailQueueProcessor {

    private static final Logger log = LoggerFactory.getLogger(EmailQueueProcessor.class);

    private final EmailService emailService;

    public EmailQueueProcessor(EmailService emailService) {
        this.emailService = emailService;
    }

    @Scheduled(fixedDelay = 30000) // Every 30 seconds
    @Async
    public void processEmailQueue() {
        log.info("Processing pending emails...");
        emailService.processPendingEmails();
    }
}
```

Enable async support in your main configuration:

```java
package com.projectname.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
}
```

---

## Step 4: Validate Migration

### 4.1 Build Project

```bash
mvn compile
```

Use `get_errors` to identify and fix any issues.

### 4.2 Run Application

```bash
mvn spring-boot:run
```

### 4.3 Validate Business Logic

Check each business rule documented in Phase 2:

| Business Rule | Expected Behavior | Status |
|---------------|-------------------|--------|
| [Rule 1] | [Expected] | ✅/❌ |

---

## Step 5: Containerization (If Required)

If specified in Phase 1, create Docker support:

```dockerfile
# Dockerfile
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apt-get update && apt-get install -y maven \
    && mvn clean package -DskipTests

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
EXPOSE 8080

# Run as non-root user
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser

COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Step 6: Update Status Report

Update `reports/Report-Status.md`:

```markdown
## Phase 3 Summary

- **Migration Status**: Complete
- **Files Migrated**: [X]
- **Build Status**: ✅ Passing
- **Business Logic Preserved**: ✅ All rules migrated

## Next Step

Run `/phase4-generateinfra` to create Azure infrastructure.
```

---

## Rules & Constraints

### Code Reading
- Read **2000 lines at a time** for sufficient context
- Use `semantic_search` for cross-file pattern discovery
- Always read PHP source before writing Java equivalent

### Build Frequently
- Build after each wave
- Use `get_errors` to catch issues early
- Fix errors before proceeding

### Business Logic Priority
- **CRITICAL**: Preserve ALL business logic from PHP
- Reference exact locations from Phase 2 plan
- Add comments noting which PHP method/line was migrated

### Do NOT
- Do NOT skip steps in the wave order
- Do NOT deviate from the Phase 2 plan
- Do NOT ignore build errors

---

## Deliverables

At the end of Phase 3:

1. ✅ Complete Java / Spring Boot project structure
2. ✅ All JPA entities migrated with relationships
3. ✅ All services migrated with business logic
4. ✅ All controllers migrated with actions
5. ✅ All views migrated from Blade/Twig to Thymeleaf
6. ✅ Filters, interceptors, and scheduled tasks
7. ✅ Static assets copied
8. ✅ Application builds successfully
9. ✅ Docker support (if required)
10. ✅ `reports/Report-Status.md` updated

**Next Step**: `/phase4-generateinfra` to create Azure infrastructure.
