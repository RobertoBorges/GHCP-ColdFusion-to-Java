---
name: jpa-hibernate-migration
description: JPA / Hibernate migration patterns for ColdFusion CF-ORM / DataMgr / cfquery to Spring Data JPA. Use when converting CF-ORM persistent CFCs to JPA entities, migrating relationships, creating repositories, or setting up Flyway/Liquibase migrations.
---

# JPA / Hibernate Migration from ColdFusion Data Access

> Use this skill when migrating ColdFusion data access (CF-ORM persistent CFCs, DataMgr, or hand-written
> `<cfquery>` DAOs) to JPA / Hibernate with Spring Data JPA. Applies to Adobe ColdFusion, Lucee, and
> legacy Railo/BlueDragon engines. MySQL is the primary source database.

## When to Use This Skill

- Converting CF-ORM persistent CFCs (`persistent="true"` + `cfproperty`) to JPA entities
- Migrating DataMgr models and hand-written `<cfquery>` DAOs to JPA/Hibernate
- Setting up relationships (one-to-many, many-to-one, many-to-many)
- Creating Spring Data JPA repositories from CFC finder methods
- Creating and running database migrations with Flyway or Liquibase
- Configuring soft deletes, timestamps, and finder queries

> Note: CF-ORM (backed by Hibernate) maps most directly to JPA. DataMgr and `<cfquery>` DAOs have no ORM
> metadata, so infer columns/relationships from the SQL and table structure (MySQL schema is the source of truth).

## Template Files

See the [templates](./templates/) directory:
- [Entity.java](./templates/Entity.java) - Sample JPA entity with relationships
- [Repository.java](./templates/Repository.java) - Spring Data JPA repository with query methods
- [Migration commands](./templates/migration-commands.md) - Flyway/Liquibase command reference

## CF-ORM / CFML to JPA Mapping

### Component & Property Definitions

| ColdFusion (CF-ORM / cfproperty) | JPA / Hibernate |
|----------------------------------|-----------------|
| `component persistent="true" table="users"` | `@Entity` + `@Table(name = "users")` |
| `property name="id" fieldtype="id"` | `@Id` |
| `property ... generator="identity"` / `"increment"` | `@GeneratedValue(strategy = IDENTITY)` |
| `property ... generator="assigned"` | `@GeneratedValue` omitted (assign manually) |
| `property name="userId" column="user_id"` | `@Column(name = "user_id")` |
| `property ... ormtype="string" length="255"` | `String` + `@Column(length = 255)` |
| `property ... notnull="true"` | `@Column(nullable = false)` |
| `property ... unique="true"` | `@Column(unique = true)` |
| `property ... ormtype="big_decimal"` | `BigDecimal` (use `@Column(precision, scale)`) |
| `property ... persistent="false"` | `@Transient` |
| `property ... formula="..."` | `@Formula("...")` |
| No CF-ORM metadata (DataMgr / `<cfquery>` DAO) | Infer `@Column` fields from the SQL/table schema |
| Hidden/internal fields in serialization | `@JsonIgnore` or DTO projection |

### Relationship Mapping

| ColdFusion (CF-ORM fieldtype) | JPA / Hibernate |
|-------------------------------|-----------------|
| `fieldtype="one-to-one"` | `@OneToOne` + `@JoinColumn` (or `mappedBy` on the inverse side) |
| `fieldtype="one-to-many" fkcolumn="..."` | `@OneToMany(mappedBy = "parent")` — use `List<Child>` |
| `fieldtype="many-to-one" fkcolumn="..."` | `@ManyToOne` + `@JoinColumn(name = "fk_column")` |
| `fieldtype="many-to-many" linktable="..."` | `@ManyToMany` + `@JoinTable` — use `Set<Other>` |
| Manual join in `<cfquery>` (DataMgr relation) | Model the relationship explicitly with the annotations above |
| Polymorphic table (no CF-ORM equivalent) | `@Inheritance(strategy = SINGLE_TABLE)` + `@DiscriminatorColumn` |
| Join across a link table in SQL | `@Query` with `JOIN` or intermediate entity navigation |

### Common Patterns

#### Soft Deletes

```java
// Entity — replaces a "deletedAt"/"is_deleted" column convention in CFML
@Entity
@Table(name = "products")
@SQLRestriction("deleted_at IS NULL") // Hibernate 6.3+ (replaces @Where)
public class Product {

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.deletedAt = null;
    }
}

// To query including soft-deleted records:
// Use a native query or a separate repository method with @Query
// @Query(value = "SELECT * FROM products WHERE id = :id", nativeQuery = true)
```

#### Timestamps

```java
// Option 1: Hibernate annotations (simpler)
@CreationTimestamp
@Column(name = "created_at", updatable = false)
private LocalDateTime createdAt;

@UpdateTimestamp
@Column(name = "updated_at")
private LocalDateTime updatedAt;

// Option 2: Spring Data JPA Auditing (more flexible)
// Enable with @EnableJpaAuditing on a @Configuration class
@CreatedDate
@Column(name = "created_at", updatable = false)
private LocalDateTime createdAt;

@LastModifiedDate
@Column(name = "updated_at")
private LocalDateTime updatedAt;

// Entity must be annotated with @EntityListeners(AuditingEntityListener.class)
```

#### CFC Finder Methods → Repository Query Methods

```java
// CFML CFC finders, e.g.:
//   productService.getActiveProducts()
//   productService.getByCategory(id)
//   productService.getByPriceRange(min, max)

// Spring Data JPA — derived query methods:
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByActiveTrue();                        // getActiveProducts()
    List<Product> findByCategoryId(Long categoryId);         // getByCategory(id)
    List<Product> findByPriceBetween(BigDecimal min, BigDecimal max); // getByPriceRange(min,max)

    // Composed finders via method name convention:
    List<Product> findByActiveTrueAndCategoryIdAndPriceBetween(
        Long categoryId, BigDecimal min, BigDecimal max);
}

// For dynamic <cfquery> filters (WHERE clauses built with <cfif>), use Specifications:
public class ProductSpecifications {

    public static Specification<Product> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<Product> inCategory(Long categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> cb.between(root.get("price"), min, max);
    }
}

// Usage:
// productRepository.findAll(isActive().and(inCategory(5)).and(priceBetween(10, 100)));
```

#### Computed Getters / UDFs → Computed Getters

```java
// CFML: public string function getFormattedPrice() { return dollarFormat(variables.price); }
// JPA: @Transient method (not persisted)

@Transient
public String getFormattedPrice() {
    return NumberFormat.getCurrencyInstance(Locale.US).format(price);
}
```

#### CFC Setter Logic → Setter Logic

```java
// CFML: public void function setName(name) { variables.name = name;
//                                            variables.slug = reReplace(lCase(name), "[^a-z0-9]+", "-", "all"); }
// JPA: logic in the setter

public void setName(String name) {
    this.name = name;
    this.slug = Slugify.slugify(name); // or manual slug generation
}
```

#### ORM Events → @EntityListeners or Spring Events

```java
// CF-ORM: preInsert()/postInsert()/preUpdate() methods on the CFC,
//         or a global ORM event handler (this.ormsettings.eventHandler in Application.cfc)

// Option 1: JPA Lifecycle Callbacks
@Entity
public class Product {
    @PrePersist
    public void onPrePersist() {         // replaces preInsert()
        if (this.slug == null) {
            this.slug = generateSlug(this.name);
        }
    }

    @PreUpdate
    public void onPreUpdate() {           // replaces preUpdate()
        // pre-update logic
    }
}

// Option 2: Separate EntityListener class (replaces a global ORM event handler CFC)
@Entity
@EntityListeners(ProductEntityListener.class)
public class Product { ... }

public class ProductEntityListener {
    @PrePersist
    public void prePersist(Product product) { ... }
}

// Option 3: Spring Application Events
// Publish: applicationEventPublisher.publishEvent(new ProductCreatedEvent(product));
// Listen: @EventListener or @TransactionalEventListener
```

## Data Type Mapping

| MySQL column (CFML `ormtype`) | Java | JPA Column |
|-------------------------------|------|------------|
| `int` (`ormtype="integer"`) | `Integer` | `@Column` (default) |
| `bigint` (`ormtype="long"`) | `Long` | `@Column` |
| `decimal(10,2)` (`ormtype="big_decimal"`) | `BigDecimal` | `@Column(precision = 10, scale = 2)` |
| `varchar(255)` (`ormtype="string"`) | `String` | `@Column(length = 255)` |
| `text` (`ormtype="text"`) | `String` | `@Column(columnDefinition = "TEXT")` or `@Lob` |
| `boolean` / `tinyint(1)` (`ormtype="boolean"`) | `boolean` / `Boolean` | `@Column` |
| `datetime` (`ormtype="timestamp"`) | `LocalDateTime` | `@Column` |
| `date` (`ormtype="date"`) | `LocalDate` | `@Column` |
| `time` (`ormtype="time"`) | `LocalTime` | `@Column` |
| `timestamp` (`ormtype="timestamp"`) | `Instant` or `LocalDateTime` | `@Column` |
| `json` (stored as text in CFML) | `String` + `AttributeConverter` | `@Column(columnDefinition = "JSON")` |
| `enum` (CFML string/list) | Java `enum` | `@Enumerated(EnumType.STRING)` |
| `float` / `double` (`ormtype="float"/"double"`) | `Double` | `@Column` |
| `blob` (`ormtype="binary"`) | `byte[]` | `@Lob` |
| `char(36)` UUID (`createUUID()`) | `UUID` / `String` | `@Column(columnDefinition = "uuid")` |

> **UUID note:** CFML `createUUID()` produces a 35-char hyphenated value (8-4-4-16). Normalize to a
> standard 36-char UUID during migration, or keep as `String` if the legacy format must be preserved.

## Migration Commands

See the [migration commands reference](./templates/migration-commands.md) for the full comparison.

```bash
# Flyway — Create migration file manually:
# src/main/resources/db/migration/V1__create_products_table.sql

# Apply all pending migrations
mvn flyway:migrate

# Show migration status
mvn flyway:info

# Liquibase equivalent:
mvn liquibase:update
mvn liquibase:status
```

## Best Practices

1. **Use DTOs for API boundaries** — Never expose JPA entities directly in REST responses; map to records
2. **Lazy loading by default** — Use `FetchType.LAZY` for all relationships, fetch eagerly with `JOIN FETCH` or `@EntityGraph` when needed
3. **Avoid N+1 queries** — Use `@EntityGraph`, `JOIN FETCH` in JPQL, or batch fetching (`@BatchSize`); watch for `<cfloop>`-over-query patterns that hid N+1s in CFML
4. **@Transactional boundaries** — Place `@Transactional` on service methods, not repositories (replaces `<cftransaction>`)
5. **Use Flyway for migrations** — Never modify schema manually; version all changes as SQL migration files (replaces ad-hoc `.sql` scripts / CF Admin datasource changes)
6. **Use Spring Data JPA repositories** — Let Spring generate boilerplate; add `@Query` only for complex queries that were dynamic `<cfquery>` blocks
7. **equals/hashCode** — Implement based on business keys (e.g., slug, email), not auto-generated IDs
8. **Projections** — Use interface-based or record-based projections for read-only queries to reduce data transfer
9. **Auditing** — Use Spring Data JPA Auditing (`@EnableJpaAuditing`) for automatic created/updated timestamps
10. **Always parameterize** — Every `<cfqueryparam>` becomes a bound parameter; never concatenate user input into JPQL/SQL (audit dynamic `<cfquery>` for SQL injection during migration)
11. **Connection pooling** — Spring Boot auto-configures HikariCP; tune pool size in `application.yml` (replaces CF Admin datasource connection settings)
