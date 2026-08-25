// =============================================================================
// Spring Boot Application Entry Point
// Part of the PHP-to-Java Migration Framework
// =============================================================================
// This is the entry point for Spring Boot applications.
// Replaces Laravel's bootstrap/app.php, public/index.php, and config/app.php
// =============================================================================

package com.example.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class — the entry point for the Spring Boot application.
 *
 * @SpringBootApplication combines:
 *   - @Configuration      → marks this as a source of bean definitions
 *   - @EnableAutoConfiguration → auto-configures Spring Boot based on classpath
 *   - @ComponentScan      → scans this package and sub-packages for @Component,
 *                            @Service, @Controller, @Repository beans
 *
 * This replaces:
 *   - Laravel's bootstrap/app.php (application bootstrapping)
 *   - Laravel's public/index.php (HTTP entry point)
 *   - Laravel's config/app.php (app configuration — now in application.yml)
 *   - Laravel's Service Providers auto-discovery (Spring uses component scanning)
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // ==========================================================================
    // WHAT GOES HERE vs. @Configuration CLASSES
    // ==========================================================================
    //
    // Put in Application.java:
    //   - The main() method (required)
    //   - Simple @Bean definitions for app-wide cross-cutting concerns
    //   - @EventListener for application lifecycle events
    //
    // Put in separate @Configuration classes:
    //   - Security config (SecurityConfig.java)
    //   - Web MVC config (WebMvcConfig.java)
    //   - Cache config (CacheConfig.java)
    //   - Data source / JPA config (if customization needed)
    //   - External service client configuration
    //   - @ConfigurationProperties bindings
    //
    // Spring Boot auto-configures most things based on your dependencies.
    // You only need @Configuration classes to OVERRIDE defaults.
    // ==========================================================================
}

// =============================================================================
// application.yml — Main Configuration
// Replaces: .env, config/app.php, config/database.php, config/cache.php
// Place in: src/main/resources/application.yml
// =============================================================================

/*
# application.yml

spring:
  application:
    name: my-app                           # APP_NAME in .env

  # Database — replaces config/database.php + DB_* in .env
  datasource:
    url: jdbc:postgresql://localhost:5432/myapp
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:secret}
    driver-class-name: org.postgresql.Driver

  # JPA / Hibernate — replaces Eloquent config
  jpa:
    hibernate:
      ddl-auto: validate                   # Use Flyway for migrations, not auto-DDL
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        default_batch_fetch_size: 16       # Avoid N+1 queries

  # Flyway — replaces database/migrations/
  flyway:
    enabled: true
    locations: classpath:db/migration

  # Thymeleaf — replaces Blade config
  thymeleaf:
    cache: true                            # Set false in dev profile
    prefix: classpath:/templates/
    suffix: .html

  # Cache — replaces config/cache.php
  cache:
    type: caffeine                         # or redis
    caffeine:
      spec: maximumSize=500,expireAfterWrite=3600s

  # Mail — replaces config/mail.php + MAIL_* in .env
  mail:
    host: ${MAIL_HOST:smtp.mailtrap.io}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME:}
    password: ${MAIL_PASSWORD:}

  # Session — replaces config/session.php
  session:
    store-type: jdbc                       # or redis
    timeout: 30m

# Server config
server:
  port: 8080
  servlet:
    context-path: /

# Actuator — replaces Laravel Telescope
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics

# Logging — replaces config/logging.php
logging:
  level:
    root: INFO
    com.example.app: DEBUG
    org.hibernate.SQL: DEBUG               # Show SQL queries in dev
*/

// =============================================================================
// pom.xml Starter Dependencies
// Replaces: composer.json require section
// =============================================================================

/*
<!-- Core Spring Boot dependencies (replaces laravel/framework) -->
<dependencies>
    <!-- Spring Boot Web MVC (replaces laravel/framework routing + HTTP) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Thymeleaf templates (replaces Blade views) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>

    <!-- Spring Data JPA + Hibernate (replaces Eloquent ORM) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Spring Security (replaces laravel/framework auth) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- Bean Validation (replaces Laravel validation) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Spring Boot Mail (replaces phpmailer or Laravel Mail) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>

    <!-- Flyway Migrations (replaces Laravel migrations) -->
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>

    <!-- PostgreSQL Driver (or mysql-connector-j for MySQL) -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Spring Boot DevTools (replaces barryvdh/laravel-debugbar) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>

    <!-- Spring Boot Actuator (replaces laravel/telescope) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- Test dependencies -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
*/
