---
name: jpa-hibernate-migration
description: JPA / Hibernate migration patterns for PHP Eloquent/Doctrine to Spring Data JPA. Use when converting Eloquent models to JPA entities, migrating relationships, creating repositories, or setting up Flyway/Liquibase migrations.
---

# JPA / Hibernate Migration from PHP ORM

> Use this skill when migrating PHP ORM (Eloquent/Doctrine) to JPA / Hibernate with Spring Data JPA.

## When to Use This Skill

- Converting Eloquent models to JPA entities
- Migrating Doctrine entities to JPA/Hibernate
- Setting up relationships (hasMany, belongsTo, belongsToMany)
- Creating Spring Data JPA repositories
- Creating and running database migrations with Flyway or Liquibase
- Configuring soft deletes, timestamps, and scopes

## Template Files

See the [templates](./templates/) directory:
- [Entity.java](./templates/Entity.java) - Sample JPA entity with relationships
- [Repository.java](./templates/Repository.java) - Spring Data JPA repository with query methods
- [Migration commands](./templates/migration-commands.md) - Flyway/Liquibase command reference

## Eloquent to JPA Mapping

### Model Properties

| Eloquent | JPA / Hibernate |
|----------|-----------------|
| `protected $table = 'users'` | `@Table(name = "users")` |
| `protected $primaryKey = 'user_id'` | `@Id` + `@Column(name = "user_id")` |
| `public $incrementing = false` | `@GeneratedValue` omitted (assign manually) |
| `protected $keyType = 'string'` | Use `String` type for `@Id` field |
| `public $timestamps = false` | Omit `@CreationTimestamp` / `@UpdateTimestamp` |
| `protected $fillable = [...]` | No direct equivalent — use DTOs for input binding |
| `protected $guarded = [...]` | No direct equivalent — use DTOs for input binding |
| `protected $hidden = [...]` | `@JsonIgnore` on fields or use DTO projection |
| `protected $casts = [...]` | Use proper Java types or `@Convert` with `AttributeConverter` |
| `protected $appends = [...]` | `@Transient` computed getter methods |
| `protected $with = [...]` | `@EntityGraph` or `JOIN FETCH` in queries |

### Relationship Mapping

| Eloquent Method | JPA / Hibernate |
|-----------------|-----------------|
| `hasOne()` | `@OneToOne(mappedBy = "parent")` on parent, `@OneToOne` + `@JoinColumn` on child |
| `hasMany()` | `@OneToMany(mappedBy = "parent")` — use `List<Child>` |
| `belongsTo()` | `@ManyToOne` + `@JoinColumn(name = "fk_column")` |
| `belongsToMany()` | `@ManyToMany` + `@JoinTable` — use `Set<Other>` |
| `morphTo()` | `@Inheritance(strategy = InheritanceType.SINGLE_TABLE)` with `@DiscriminatorColumn` |
| `morphMany()` | `@OneToMany` with `@Inheritance` or discriminator column pattern |
| `hasManyThrough()` | `@Query` with JOIN or intermediate entity navigation |

### Common Patterns

#### Soft Deletes

```java
// Entity — replaces: use SoftDeletes;
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

// To query including soft-deleted records (withTrashed):
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

#### Scopes → Repository Query Methods

```java
// Eloquent: $query->active()->inCategory($id)->priceBetween($min, $max)

// Spring Data JPA — derived query methods:
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByActiveTrue();                        // scopeActive
    List<Product> findByCategoryId(Long categoryId);         // scopeInCategory
    List<Product> findByPriceBetween(BigDecimal min, BigDecimal max); // scopePriceRange

    // Composed scopes via method name convention:
    List<Product> findByActiveTrueAndCategoryIdAndPriceBetween(
        Long categoryId, BigDecimal min, BigDecimal max);
}

// For dynamic queries, use Specifications (similar to query scopes composition):
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

#### Accessors → Computed Getters

```java
// Eloquent: public function getFormattedPriceAttribute()
// JPA: @Transient method (not persisted)

@Transient
public String getFormattedPrice() {
    return NumberFormat.getCurrencyInstance(Locale.US).format(price);
}
```

#### Mutators → Setter Logic

```java
// Eloquent: public function setNameAttribute($value) { ... $this->attributes['slug'] = Str::slug($value); }
// JPA: logic in the setter

public void setName(String name) {
    this.name = name;
    this.slug = Slugify.slugify(name); // or manual slug generation
}
```

#### Events → @EntityListeners or Spring Events

```java
// Eloquent: static::creating(function ($model) { ... });

// Option 1: JPA Lifecycle Callbacks
@Entity
public class Product {
    @PrePersist
    public void onPrePersist() {
        if (this.slug == null) {
            this.slug = generateSlug(this.name);
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        // pre-update logic
    }
}

// Option 2: Separate EntityListener class
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

| PHP/MySQL | Java | JPA Column |
|-----------|------|------------|
| `int` | `Integer` | `@Column` (default) |
| `bigint` | `Long` | `@Column` |
| `decimal(10,2)` | `BigDecimal` | `@Column(precision = 10, scale = 2)` |
| `varchar(255)` | `String` | `@Column(length = 255)` |
| `text` | `String` | `@Column(columnDefinition = "TEXT")` or `@Lob` |
| `boolean` / `tinyint(1)` | `boolean` / `Boolean` | `@Column` |
| `datetime` | `LocalDateTime` | `@Column` |
| `date` | `LocalDate` | `@Column` |
| `time` | `LocalTime` | `@Column` |
| `timestamp` | `Instant` or `LocalDateTime` | `@Column` |
| `json` | `String` + `AttributeConverter` | `@Column(columnDefinition = "JSON")` |
| `enum` | Java `enum` | `@Enumerated(EnumType.STRING)` |
| `float` / `double` | `Double` | `@Column` |
| `blob` | `byte[]` | `@Lob` |
| `uuid` | `UUID` | `@Column(columnDefinition = "uuid")` |

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
3. **Avoid N+1 queries** — Use `@EntityGraph`, `JOIN FETCH` in JPQL, or batch fetching (`@BatchSize`)
4. **@Transactional boundaries** — Place `@Transactional` on service methods, not repositories
5. **Use Flyway for migrations** — Never modify schema manually; version all changes as SQL migration files
6. **Use Spring Data JPA repositories** — Let Spring generate boilerplate; add `@Query` only for complex queries
7. **equals/hashCode** — Implement based on business keys (e.g., slug, email), not auto-generated IDs
8. **Projections** — Use interface-based or record-based projections for read-only queries to reduce data transfer
9. **Auditing** — Use Spring Data JPA Auditing (`@EnableJpaAuditing`) for automatic created/updated timestamps
10. **Connection pooling** — Spring Boot auto-configures HikariCP; tune pool size in `application.yml`
