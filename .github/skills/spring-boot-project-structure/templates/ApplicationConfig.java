// =============================================================================
// Spring Boot Configuration Class
// Part of the ColdFusion-to-Java Migration Framework
// =============================================================================
// Centralized application configuration with @Bean definitions.
// Replaces Application.cfc bootstrap wiring, config CFCs, and settings.ini.cfm.
// =============================================================================

package com.example.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Main application configuration.
 *
 * Replaces:
 *   - Application.cfc onApplicationStart() service wiring
 *   - settings.ini.cfm / config CFC application settings
 *   - CORS handling done manually via <cfheader> in onRequestStart()
 *
 * @Configuration marks this as a source of @Bean definitions.
 * @EnableConfigurationProperties enables typed config binding.
 * @EnableAsync enables @Async method execution (replaces <cfthread>).
 * @EnableScheduling enables @Scheduled methods (replaces <cfschedule> / scheduled tasks).
 */
@Configuration
@EnableConfigurationProperties({AppProperties.class, MailProperties.class, StorageProperties.class})
@EnableAsync
@EnableScheduling
public class ApplicationConfig {

    // ==========================================================================
    // RestTemplate Bean (replaces <cfhttp>)
    //
    // CFML: <cfhttp method="GET" url="...">, <cfhttp method="POST" url="...">
    // Spring: restTemplate.getForObject(), restTemplate.postForObject()
    //
    // For reactive/non-blocking HTTP, use WebClient instead.
    // ==========================================================================

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // ==========================================================================
    // ObjectMapper Customization (replaces JSON encoding options)
    //
    // CFML: serializeJSON(data)
    // Spring: ObjectMapper handles all JSON serialization/deserialization.
    //
    // Spring Boot auto-configures ObjectMapper, but you can customize it here.
    // ==========================================================================

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Support Java 8+ date/time types (LocalDateTime, ZonedDateTime, etc.)
        mapper.registerModule(new JavaTimeModule());

        // Write dates as ISO strings, not timestamps
        // Replaces: CFML date handling in serializeJSON() / dateTimeFormat()
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }

    // ==========================================================================
    // CORS Configuration (replaces manual <cfheader> CORS in onRequestStart)
    //
    // CFML:
    //   <cfheader name="Access-Control-Allow-Origin" value="*">
    //   <cfheader name="Access-Control-Allow-Methods" value="GET,POST,PUT,DELETE">
    //
    // Spring: WebMvcConfigurer with CORS mappings.
    // For API-only apps, this may also go in SecurityConfig.
    // ==========================================================================

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:3000", "https://myapp.com")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}

// =============================================================================
// @ConfigurationProperties Classes
// Replace CFML config (settings.ini.cfm, config CFCs, this.* in Application.cfc)
// with type-safe Java classes.
//
// Usage in services:
//   @Autowired or constructor injection of AppProperties
//
// Replaces:
//   application.appName / getSetting("app.name")  → appProperties.getName()
//   application.appUrl  / getSetting("app.url")   → appProperties.getUrl()
// =============================================================================

/**
 * Application settings — replaces this.* settings / settings.ini.cfm app section.
 * Binds to 'app.*' keys in application.yml.
 *
 * application.yml:
 *   app:
 *     name: My Application
 *     url: https://myapp.com
 *     timezone: UTC
 */
@ConfigurationProperties(prefix = "app")
class AppProperties {

    private String name = "My Application";
    private String url = "https://localhost";
    private String timezone = "UTC";

    // Getters and setters (required for @ConfigurationProperties binding)
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
}

/**
 * Mail settings — replaces CF Administrator mail settings / mail config CFC.
 * Binds to 'app.mail.*' keys in application.yml.
 *
 * Note: Spring Boot already binds spring.mail.* automatically.
 * Use this for custom mail settings beyond Spring defaults.
 *
 * application.yml:
 *   app:
 *     mail:
 *       from: noreply@myapp.com
 *       from-name: My Application
 */
@ConfigurationProperties(prefix = "app.mail")
class MailProperties {

    private String from = "";
    private String fromName = "";

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    public String getFromName() { return fromName; }
    public void setFromName(String fromName) { this.fromName = fromName; }
}

/**
 * Storage settings — replaces file-path settings (this.mappings / settings.ini.cfm).
 * Binds to 'app.storage.*' keys in application.yml.
 *
 * application.yml:
 *   app:
 *     storage:
 *       default-disk: local
 *       s3-bucket: my-bucket
 *       s3-region: us-east-1
 */
@ConfigurationProperties(prefix = "app.storage")
class StorageProperties {

    private String defaultDisk = "local";
    private String s3Bucket = "";
    private String s3Region = "us-east-1";

    public String getDefaultDisk() { return defaultDisk; }
    public void setDefaultDisk(String defaultDisk) { this.defaultDisk = defaultDisk; }
    public String getS3Bucket() { return s3Bucket; }
    public void setS3Bucket(String s3Bucket) { this.s3Bucket = s3Bucket; }
    public String getS3Region() { return s3Region; }
    public void setS3Region(String s3Region) { this.s3Region = s3Region; }
}

// =============================================================================
// Service Lifetime Reference
// =============================================================================
/*
CFML scopes vs Spring Bean Scopes:

| ColdFusion                              | Spring                  | Description                              |
|-----------------------------------------|-------------------------|------------------------------------------|
| CFC stored in `application` scope        | @Scope("singleton")     | One instance for the entire app (DEFAULT) |
| createObject()/new per use               | @Scope("prototype")     | New instance every time it's requested   |
| CFC stored in `request` scope            | @Scope("request")       | One instance per HTTP request            |
| CFC stored in `session` scope            | @Scope("session")       | One instance per HTTP session            |

IMPORTANT: Spring's DEFAULT scope is singleton (one instance for the app).
Application-scope CFCs were singletons too — but many CF apps also created CFCs
per-request via createObject()/new, so double-check each component's intent.

Common patterns:
- @Service, @Repository, @Controller → Singleton (default, stateless)
- Request-scoped beans → @Scope("request") (rare, only if needed)
- Prototype beans → @Scope("prototype") (rare, for stateful objects)

Since most Spring beans are singleton, they MUST be thread-safe — the same rule
that applied to application-scope CFCs (use var/local scoping, avoid shared mutable state).
Do NOT store request-specific state in fields of @Service or @Controller classes.
*/

// =============================================================================
// Usage in Services — inject @ConfigurationProperties
// =============================================================================
/*
@Service
public class EmailService {

    private final MailProperties mailProperties;
    private final JavaMailSender mailSender;

    // CFML: application.mailFrom / getSetting("mail.from")
    // Spring: mailProperties.getFrom()
    public EmailService(MailProperties mailProperties, JavaMailSender mailSender) {
        this.mailProperties = mailProperties;
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(String to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getFrom());
        message.setTo(to);
        message.setSubject("Welcome!");
        message.setText("Welcome to " + appProperties.getName());
        mailSender.send(message);
    }
}
*/
