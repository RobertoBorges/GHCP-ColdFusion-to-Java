// =============================================================================
// Spring Boot Application Entry Point
// Part of the ColdFusion-to-Java Migration Framework
// =============================================================================
// This is the entry point for Spring Boot applications.
// Replaces ColdFusion's Application.cfc / Application.cfm bootstrap and the
// web-root request dispatch (index.cfm / onRequest).
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
 *   - Application.cfc onApplicationStart() (application bootstrapping)
 *   - The web-root HTTP entry point (index.cfm / onRequest dispatch)
 *   - CF Administrator / settings.ini.cfm app configuration — now in application.yml
 *   - Manual application-scope CFC wiring (Spring uses component scanning)
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
// Replaces: Application.cfc this.* settings, settings.ini.cfm, CF Administrator
//           datasource / mail / cache settings
// Place in: src/main/resources/application.yml
// =============================================================================

/*
# application.yml

spring:
  application:
    name: my-app                           # this.name in Application.cfc

  # Database — replaces the CF Administrator datasource / this.datasource
  # MySQL is shown (the common ColdFusion source DB); swap the url/driver for PostgreSQL if desired.
  datasource:
    url: jdbc:mysql://localhost:3306/myapp
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:secret}
    driver-class-name: com.mysql.cj.jdbc.Driver

  # JPA / Hibernate — replaces CF-ORM / DataMgr / <cfquery> data access
  jpa:
    hibernate:
      ddl-auto: validate                   # Use Flyway for migrations, not auto-DDL
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        default_batch_fetch_size: 16       # Avoid N+1 queries

  # Flyway — replaces hand-written .sql schema scripts / cfmigrations
  flyway:
    enabled: true
    locations: classpath:db/migration

  # Thymeleaf — replaces .cfm views / <cfoutput> rendering
  thymeleaf:
    cache: true                            # Set false in dev profile
    prefix: classpath:/templates/
    suffix: .html

  # Cache — replaces <cfcache> / CF Administrator cache settings
  cache:
    type: caffeine                         # or redis
    caffeine:
      spec: maximumSize=500,expireAfterWrite=3600s

  # Mail — replaces <cfmail> + CF Administrator mail server settings
  mail:
    host: ${MAIL_HOST:smtp.mailtrap.io}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME:}
    password: ${MAIL_PASSWORD:}

  # Session — replaces this.sessionManagement / this.sessionTimeout
  session:
    store-type: jdbc                       # or redis
    timeout: 30m

# Server config
server:
  port: 8080
  servlet:
    context-path: /

# Actuator — replaces the CF Server Monitor
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics

# Logging — replaces <cflog> / writeLog() destinations and CF log settings
logging:
  level:
    root: INFO
    com.example.app: DEBUG
    org.hibernate.SQL: DEBUG               # Show SQL queries in dev
*/

// =============================================================================
// pom.xml Starter Dependencies
// Replaces: JARs dropped in /lib, CF mappings, or box.json dependencies
// =============================================================================

/*
<!-- Core Spring Boot dependencies -->
<dependencies>
    <!-- Spring Boot Web MVC (replaces .cfm request dispatch / onRequest) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Thymeleaf templates (replaces .cfm views / <cfoutput>) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>

    <!-- Spring Data JPA + Hibernate (replaces CF-ORM / DataMgr / <cfquery>) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Spring Security (replaces <cflogin> / <cfloginuser>) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- Bean Validation (replaces isValid() / <cfparam> type checks) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Spring Boot Mail (replaces <cfmail>) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>

    <!-- Flyway Migrations (replaces hand-written .sql scripts / cfmigrations) -->
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>

    <!-- MySQL Driver (or postgresql for PostgreSQL) -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Spring Boot DevTools (replaces <cfdump> / CF debugging output) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>

    <!-- Spring Boot Actuator (replaces the CF Server Monitor) -->
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
