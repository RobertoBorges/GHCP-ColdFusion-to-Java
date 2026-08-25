---
name: coldfusion-to-java-mapping
description: ColdFusion (CFML) to Java 21 / Spring Boot 3.x code mapping reference. Use when converting ColdFusion code patterns to Java equivalents including framework mapping, authentication, templates (CFML/CFOutput to Thymeleaf), packages (built-in CF tags to Maven/Gradle libraries), and validation rules.
---

# ColdFusion (CFML) to Java 21 / Spring Boot 3 Mapping Reference

> Use this skill when migrating ColdFusion code to Java 21 with Spring Boot 3.x. It provides direct
> mappings for common CFML patterns. It covers Adobe ColdFusion, Lucee, and legacy Railo/BlueDragon
> engines, and both tag-based CFML and `cfscript` syntax, including legacy `Application.cfm`-style apps.

## Framework Mapping

| ColdFusion Framework | Java Equivalent |
|----------------------|-----------------|
| Vanilla CFML (`Application.cfm` / `Application.cfc` + `.cfm` pages) | Spring Boot MVC (`@Controller` + Thymeleaf) |
| FW/1 (Framework One) | Spring Boot MVC |
| ColdBox (HMVC) | Spring Boot MVC (WireBox → Spring DI, interceptors → filters/AOP) |
| Fusebox | Spring Boot MVC |
| Mach-II | Spring Boot MVC (event-driven → controllers) |
| Model-Glue | Spring Boot MVC |
| CFWheels | Spring Boot MVC |
| Taffy / ColdBox REST | Spring Boot REST (`@RestController`) |

## Architecture Pattern Mapping

| CFML Pattern | Java Equivalent |
|--------------|-----------------|
| CFC (`.cfc` component) | Java class (`@Service` / `@Component` / entity, depending on role) |
| `.cfm` page (controller + view combined) | `@Controller` method + Thymeleaf template (separate concerns) |
| `<cffunction>` / `cfscript` `function` | Java method |
| Application-scope singleton CFC (`application.userService = createObject(...)`) | Spring singleton `@Service` bean |
| `Application.cfc` lifecycle methods | Spring lifecycle + filters/interceptors/listeners (see table below) |
| Custom tags (`<cf_tag>` / `<cfmodule>`) | Thymeleaf fragments or tag/dialect helpers |
| UDFs in `includes/udf.cfm` | Utility classes / `@Component` helper beans |

## Application.cfc Lifecycle Mapping

| CFML (`Application.cfc` / `Application.cfm`) | Java / Spring |
|----------------------------------------------|----------------|
| `onApplicationStart()` | `@PostConstruct` bean / `ApplicationRunner` / `CommandLineRunner` |
| `onSessionStart()` | `HttpSessionListener.sessionCreated()` |
| `onRequestStart()` | `Filter` / `HandlerInterceptor.preHandle()` |
| `onRequest()` | Handled by `DispatcherServlet` (framework-managed) |
| `onRequestEnd()` | `HandlerInterceptor.afterCompletion()` |
| `onError()` | `@ControllerAdvice` + `@ExceptionHandler` |
| `onMissingTemplate()` | Custom `ErrorController` / 404 handler |
| `this.*` settings (name, sessionManagement, datasource) | `application.yml` + config classes |

## Data Access Mapping

| ColdFusion | Java |
|------------|------|
| `<cfquery>` / `queryExecute()` | Spring Data JPA repositories or `JdbcTemplate` |
| `<cfqueryparam>` | Bind parameters (`?`/named params) in JPA/JdbcTemplate — always parameterize |
| `<cfstoredproc>` / `<cfprocparam>` | `@Procedure` (Spring Data) or `JdbcTemplate.call()` |
| DataMgr (ORM abstraction) | Spring Data JPA / Hibernate |
| CF-ORM (`persistent="true"` CFC + `cfproperty`) | JPA `@Entity` + mapped fields |
| Query of Queries (QoQ) | Java Streams over result lists, or SQL |
| `queryNew()` / query manipulation | `List<Map>` / `List<Record>` / DTO lists |
| CF Admin datasource | `spring.datasource.*` in `application.yml` |
| Manual `.sql` scripts / schema | Flyway or Liquibase migrations |

## Authentication Mapping

| ColdFusion | Java |
|------------|------|
| `<cflogin>` / `<cfloginuser>` | Spring Security form login + `AuthenticationManager` |
| `<cflogout>` | Spring Security logout handler |
| `isUserLoggedIn()` | `Authentication.isAuthenticated()` / `sec:authorize="isAuthenticated()"` |
| `isUserInRole("admin")` | `hasRole('ADMIN')` / `@PreAuthorize("hasRole('ADMIN')")` |
| `getAuthUser()` | `SecurityContextHolder` / `@AuthenticationPrincipal` |
| `session.user` struct | Spring Security principal + `HttpSession` |
| Persistent-login cookie (encrypted) | Spring Security remember-me token |
| `cfloginuser ... roles="user,admin"` | `GrantedAuthority` list from `UserDetailsService` |
| Hash/encrypt password (`hash()`, `bcrypt`) | `BCryptPasswordEncoder` |
| Entra ID (Azure AD) | Spring Security + MSAL4J / `spring-cloud-azure-starter-active-directory` |

## Dependency Injection & Scopes

| ColdFusion | Java |
|------------|------|
| `createObject("component","cfcs.user").init()` / `new cfcs.user()` | Spring bean + constructor injection / `@Autowired` |
| Application-scope CFC singletons | Spring singleton beans (default scope) |
| ColdBox WireBox / DI/1 | Spring `ApplicationContext` (built-in IoC) |
| `application` scope | Spring singleton beans / `@Configuration` state |
| `session` scope | `HttpSession` / `@SessionScope` beans |
| `request` scope | Request attributes / `@RequestScope` beans |
| `server` scope | Application-wide singletons / static config |
| `variables` scope (CFC instance) | Instance fields (keep singletons stateless & thread-safe) |
| `client` / `cookie` scope | Cookies / persistent store (DB, Redis) |

## Template Syntax Mapping (CFML → Thymeleaf)

| CFML | Thymeleaf (Java) |
|------|------------------|
| `<cfoutput>#var#</cfoutput>` | `th:text="${var}"` (escaped) |
| `#encodeForHTML(var)#` | `th:text="${var}"` (Thymeleaf escapes by default) |
| `#var#` raw HTML output | `th:utext="${var}"` (unescaped — use with care) |
| `<cfif cond>` … `</cfif>` | `th:if="${cond}"` |
| `<cfelse>` | second element with `th:unless` / `th:if`, or `th:switch` |
| `<cfloop query="q">` | `th:each="row : ${q}"` |
| `<cfloop array="#items#" index="i">` | `th:each="i : ${items}"` |
| `<cfloop from="1" to="10" index="i">` | `th:each="i : ${#numbers.sequence(1,10)}"` |
| `<cfinclude template="header.cfm">` | `th:insert="~{fragments/header}"` / `th:replace` |
| Custom tag `<cf_widget>` / `<cfmodule template="...">` | Thymeleaf fragment with parameters |
| `<cfset x = ...>` (in view) | Move to controller/service; expose via `Model` |
| `<cfform>` / `<cfinput>` | Thymeleaf `<form th:action th:object>` + Spring binding |
| `<cfsavecontent variable="x">` | Assemble in controller or use fragment |
| `##` (escaped hash) | literal `#` |
| `writeOutput()` / `<cfoutput>` in `.cfm` | `Model.addAttribute()` + Thymeleaf expression |
| `URLEncodedFormat()` | `th:href="@{/path(param=${v})}"` |
| `<cfloop list="#csv#" index="i">` | split to `List` in controller, `th:each` |

## Package / Built-in Tag Mapping (CFML → Maven/Gradle)

| ColdFusion tag/feature | Maven/Gradle Dependency (Java) |
|------------------------|--------------------------------|
| `<cfmail>` / `cfmailpart` | `org.springframework.boot:spring-boot-starter-mail` (`JavaMailSender`) |
| `<cfhttp>` | `java.net.http.HttpClient` (JDK 11+) or Spring `WebClient` / `RestTemplate` |
| `<cffile>` / `<cfdirectory>` | `java.nio.file.Files` / Spring `Resource` |
| `<cfdocument>` / `<cfpdf>` | OpenPDF, Flying Saucer, or `com.itextpdf` |
| `<cfspreadsheet>` | Apache POI (`org.apache.poi:poi-ooxml`) |
| `<cfchart>` | JFreeChart, or a front-end charting library |
| `<cfimage>` / `imageResize()` | `net.coobird:thumbnailator` or `org.imgscalr:imgscalr-lib` |
| `<cfldap>` | `org.springframework.ldap:spring-ldap-core` |
| `<cfftp>` | `commons-net:commons-net` |
| `<cfzip>` | `java.util.zip` (JDK built-in) |
| `<cfthread>` | `@Async` + `ThreadPoolTaskExecutor` / `CompletableFuture` |
| `<cfschedule>` / scheduled tasks | `@Scheduled` (cron expressions) |
| `<cfcache>` / `cachePut()` / `cacheGet()` | Spring Cache abstraction (`@Cacheable`, Caffeine/Redis) |
| `serializeJSON()` / `deserializeJSON()` | Jackson `ObjectMapper` (built-in with Spring Boot) |
| `<cfwddx>` / XML (`xmlParse()`) | Jackson XML / JAXB |
| `createObject("java", "…")` / JavaLoader | Native Java — add the JAR as a Maven/Gradle dependency |
| CFX custom tags (C++/Java) | Native Java component / library |
| Event Gateways (Adobe CF) | Spring Integration / message listeners (JMS, Service Bus) |
| `<cfhtmltopdf>` | Flying Saucer / headless Chrome (Playwright) |

## Configuration Mapping

| ColdFusion | Java |
|------------|------|
| `Application.cfc` `this.*` settings | `application.yml` / `application.properties` |
| `settings.ini` / config CFC (`getSettings()`) | `@ConfigurationProperties` classes + `application.yml` |
| CF Administrator datasource | `spring.datasource.*` |
| `this.datasource` | `spring.datasource` (single) / multiple `DataSource` beans |
| `this.mappings` (per-app mappings) | Java package structure / classpath |
| `this.sessionManagement` / `this.sessionTimeout` | Spring Session + `server.servlet.session.timeout` |
| `this.name` (application name) | `spring.application.name` |
| environment via `cgi.server_name` switch | Spring profiles (`application-{profile}.yml`) |

## Background Jobs & Async Mapping

| ColdFusion | Java |
|------------|------|
| `<cfschedule>` / scheduled tasks | `@Scheduled(cron = "...")` |
| `<cfthread action="run">` | `@Async` methods returning `void` / `CompletableFuture` |
| `cfthread` join / `threadJoin()` | `CompletableFuture.allOf(...).join()` |
| Event Gateways | Spring AMQP (RabbitMQ) / Azure Service Bus / Spring Kafka |
| Async CFCs via gateways | `@Async` + message listeners |
| Scheduled `.cfm` via CF Admin | `@Scheduled` beans or external scheduler |

## Validation Mapping

| CFML validation | Java (Jakarta Bean Validation) |
|-----------------|--------------------------------|
| `<cfparam name="x" type="string">` | Typed field + `@NotNull` where required |
| `<cfparam name="x" default="">` | Default value in DTO / `@RequestParam(defaultValue)` |
| `isValid("email", x)` | `@Email` |
| `isValid("integer", x)` / `isNumeric(x)` | Use `Integer` / `Long` / `BigDecimal` types |
| `isValid("range", x, 1, 100)` | `@Min(1) @Max(100)` / `@Range` (Hibernate) |
| `isValid("regex", x, pattern)` | `@Pattern(regexp = "...")` |
| `isValid("date", x)` / `isDate(x)` | `LocalDate` / `LocalDateTime` type + `@Past`/`@Future` |
| `len(trim(x)) EQ 0` required check | `@NotBlank` |
| `isValid("url", x)` | `@URL` (Hibernate Validator) |
| `listFind("a,b,c", x)` | Java `enum` type |
| Uniqueness check via `<cfquery>` | Custom validator + repository lookup |
| Manual `<cfif>` validation blocks | `@Valid` on DTO + annotations, `BindingResult` |

## Data Type Mapping (typeless CFML → typed Java)

| CFML value | Java |
|------------|------|
| String / numeric variable (typeless) | `String` / `Integer` / `Long` / `BigDecimal` (declare explicit types) |
| Boolean (`true`/`false`/`yes`/`no`/`1`/`0`) | `boolean` / `Boolean` |
| Date/time (`now()`, `createDate()`) | `LocalDate` / `LocalDateTime` / `ZonedDateTime` |
| Struct (`{}` / `structNew()`) | `Map<String,Object>`, or a typed `record` / POJO |
| Array (`[]` / `arrayNew()`) | `List<T>` |
| Query object (result of `<cfquery>`) | `List<Entity>` / `List<Map<String,Object>>` |
| List (comma-delimited string) | `List<String>` (split) |
| `numberFormat()` / `dollarFormat()` | `NumberFormat` / `DecimalFormat` |
| `dateFormat()` / `timeFormat()` | `DateTimeFormatter` |

## Code Examples

See the [examples](./examples/) directory for sample conversions:
- [Controller example](./examples/controller-example.java) - CFML `.cfm` page / CFC handler to Spring Boot controller
- [Service example](./examples/service-example.java) - Application-scope CFC service to Spring Boot service
- [Model example](./examples/model-example.java) - CFC / DataMgr / CF-ORM to JPA entity
