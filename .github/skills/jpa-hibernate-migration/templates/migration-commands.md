# Database Migration Commands Reference

> Part of the ColdFusion-to-Java Migration Framework

ColdFusion applications rarely ship with a single standard schema-migration tool. In practice, CFML
teams manage the database with **hand-written `.sql` scripts** (checked into source control), **DataMgr's
auto-create/alter** features, CF Administrator datasource changes, or the **cfmigrations** CommandBox
module (`migrate up/down`). This document maps those practices to **Flyway** and **Liquibase**, the two
standard migration tools for Spring Boot. MySQL is used for the examples.

## Migration Tool Comparison

| Feature | ColdFusion (cfmigrations / manual) | Flyway | Liquibase |
|---------|------------------------------------|--------|-----------|
| File format | `.cfc` migrations or raw `.sql` scripts | SQL files | XML/YAML/SQL |
| Naming | `YYYYMMDDHHMMSS_description.cfc` / ad-hoc `.sql` | `V1__description.sql` | `changelog-*.xml` |
| Version tracking | `cfmigrations` table (or none, if manual) | `flyway_schema_history` | `databasechangelog` |
| Rollback support | `down()` in cfmigrations (or manual reverse script) | Paid (Teams edition) | Built-in |
| Spring Boot integration | N/A | Auto-configured | Auto-configured |

## Flyway (Recommended for Spring Boot)

### Creating Migrations

| ColdFusion | Flyway |
|------------|--------|
| cfmigrations: `migrate create create_products_table` (or hand-write a `.sql` script) | Create file: `V1__create_products_table.sql` |
| cfmigrations: `migrate create add_status_to_products` | Create file: `V2__add_status_to_products.sql` |

```
Flyway migration file location:
  src/main/resources/db/migration/

Naming convention:
  V{version}__{description}.sql    — versioned (applied once)
  R__{description}.sql             — repeatable (applied when changed)

Examples:
  V1__create_products_table.sql
  V2__add_status_to_products.sql
  V3__create_product_tag_table.sql
  R__create_views.sql              — repeatable migration
```

Sample migration file (`V1__create_products_table.sql`):

```sql
-- V1__create_products_table.sql
-- Equivalent to: a hand-written CREATE TABLE script or DataMgr auto-create for the products table

CREATE TABLE products (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    price       DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    stock_quantity INT NOT NULL DEFAULT 0,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    metadata    TEXT,
    status      VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    category_id BIGINT NOT NULL,
    created_by_user_id BIGINT,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP,
    deleted_at  TIMESTAMP,

    CONSTRAINT fk_product_category FOREIGN KEY (category_id)
        REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT fk_product_created_by FOREIGN KEY (created_by_user_id)
        REFERENCES users(id) ON DELETE SET NULL,

    INDEX idx_product_slug (slug),
    INDEX idx_product_category (category_id),
    INDEX idx_product_active (is_active)
);
```

### Running Migrations

| ColdFusion | Flyway (Maven) | Flyway (Gradle) |
|------------|----------------|-----------------|
| cfmigrations: `migrate up` (or run `.sql` by hand) | `mvn flyway:migrate` | `gradle flywayMigrate` |
| cfmigrations: `migrate up` + seed data script | `mvn flyway:migrate` + run seeder | `gradle flywayMigrate` + run seeder |

```bash
# Apply all pending migrations (Maven)
mvn flyway:migrate

# Apply all pending migrations (Gradle)
gradle flywayMigrate

# With Spring Boot — migrations run automatically on startup
# Configure in application.yml:
#   spring.flyway.enabled: true
#   spring.flyway.locations: classpath:db/migration
```

### Rolling Back Migrations

| ColdFusion | Flyway |
|------------|--------|
| cfmigrations: `migrate down` | `mvn flyway:undo` (Teams edition only) |
| cfmigrations: `migrate down` (repeat) | Create reverse migration manually |
| cfmigrations: `migrate fresh` (drop + re-run) | `mvn flyway:clean` (drops everything!) |
| cfmigrations: `migrate fresh` | `mvn flyway:clean` + `mvn flyway:migrate` |

```bash
# Flyway clean — DROPS ALL OBJECTS (development only!)
# Equivalent to: cfmigrations "migrate fresh" (without re-migrating)
mvn flyway:clean

# Then re-migrate
mvn flyway:migrate

# For production rollback, create a new versioned migration:
# V4__revert_add_status_column.sql
# ALTER TABLE products DROP COLUMN status;
```

### Checking Migration Status

| ColdFusion | Flyway |
|------------|--------|
| cfmigrations: `migrate status` | `mvn flyway:info` |

```bash
# Show migration status
mvn flyway:info

# Validate migrations (check for issues)
mvn flyway:validate

# Repair metadata table (fix failed migrations)
mvn flyway:repair
```

### Spring Boot Auto-Migration Configuration

```yaml
# application.yml — Flyway runs automatically on startup
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true    # For existing databases
    baseline-version: 0          # Baseline version number
    # clean-disabled: true       # Prevent accidental clean in production
    # out-of-order: false        # Allow out-of-order migrations (dev only)
```

---

## Liquibase Alternative

### Creating Migrations

| ColdFusion | Liquibase |
|------------|-----------|
| cfmigrations: `migrate create create_products_table` (or hand-written `.sql`) | Create changelog file |

```
Liquibase file location:
  src/main/resources/db/changelog/

Naming convention:
  db.changelog-master.yaml         — master changelog (includes all others)
  db/changelog/changes/001-create-products.yaml
  db/changelog/changes/002-add-status.yaml
```

Master changelog (`db.changelog-master.yaml`):

```yaml
databaseChangeLog:
  - include:
      file: db/changelog/changes/001-create-products.yaml
  - include:
      file: db/changelog/changes/002-add-status.yaml
```

Changeset file (`db/changelog/changes/001-create-products.yaml`):

```yaml
databaseChangeLog:
  - changeSet:
      id: 1
      author: migration
      changes:
        - createTable:
            tableName: products
            columns:
              - column:
                  name: id
                  type: BIGINT
                  autoIncrement: true
                  constraints:
                    primaryKey: true
              - column:
                  name: name
                  type: VARCHAR(255)
                  constraints:
                    nullable: false
              - column:
                  name: slug
                  type: VARCHAR(255)
                  constraints:
                    nullable: false
                    unique: true
              - column:
                  name: price
                  type: DECIMAL(18,2)
                  defaultValueNumeric: 0.00
              - column:
                  name: created_at
                  type: TIMESTAMP
                  defaultValueComputed: CURRENT_TIMESTAMP
      rollback:
        - dropTable:
            tableName: products
```

Or use SQL format (`db/changelog/changes/001-create-products.sql`):

```sql
--liquibase formatted sql

--changeset migration:1
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    price DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
--rollback DROP TABLE products;
```

### Running Liquibase Commands

| ColdFusion | Liquibase (Maven) |
|------------|-------------------|
| cfmigrations: `migrate up` | `mvn liquibase:update` |
| cfmigrations: `migrate down` | `mvn liquibase:rollback -Dliquibase.rollbackCount=1` |
| cfmigrations: `migrate status` | `mvn liquibase:status` |
| cfmigrations: `migrate fresh` | `mvn liquibase:dropAll` |
| Preview SQL (manual review) | `mvn liquibase:updateSQL` |

```bash
# Apply pending changes
mvn liquibase:update

# Rollback last changeset
mvn liquibase:rollback -Dliquibase.rollbackCount=1

# Rollback to a specific tag
mvn liquibase:rollback -Dliquibase.rollbackTag=v1.0

# Show pending changesets
mvn liquibase:status

# Generate SQL without applying
mvn liquibase:updateSQL
```

### Spring Boot Liquibase Configuration

```yaml
# application.yml
spring:
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.yaml
```

---

## Seeding Data

| ColdFusion | Spring Boot |
|------------|-------------|
| Seed `.sql` script / DataMgr insert routine / `onApplicationStart` bootstrap | `CommandLineRunner` or `ApplicationRunner` bean |
| A dedicated seed CFC run once | Run specific seeder class |
| Drop/recreate + reseed (dev) | Flyway clean + migrate + seed |

```java
// Option 1: CommandLineRunner in a @Configuration class
@Configuration
@Profile("dev") // Only seed in development
public class DataSeeder {

    @Bean
    CommandLineRunner seedDatabase(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() == 0) {
                productRepository.saveAll(List.of(
                    createProduct("Widget", new BigDecimal("9.99")),
                    createProduct("Gadget", new BigDecimal("24.99"))
                ));
            }
        };
    }
}

// Option 2: Flyway callback (runs after migration)
// Create: src/main/resources/db/callback/afterMigrate.sql
// INSERT INTO products (...) VALUES (...) ON CONFLICT DO NOTHING;
```

---

## Common Migration Patterns

### Adding a Column

```sql
-- ColdFusion: hand-written ALTER script or cfmigrations "migrate create add_status_to_products"
-- Flyway: V2__add_status_to_products.sql

ALTER TABLE products ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';
CREATE INDEX idx_product_status ON products(status);
```

### Renaming a Column

```sql
-- Flyway: V3__rename_is_active_to_active.sql
ALTER TABLE products RENAME COLUMN is_active TO active;
```

### Adding a Foreign Key

```sql
-- Flyway: V4__add_brand_to_products.sql
ALTER TABLE products ADD COLUMN brand_id BIGINT;
ALTER TABLE products ADD CONSTRAINT fk_product_brand
    FOREIGN KEY (brand_id) REFERENCES brands(id) ON DELETE SET NULL;
```

### Creating a Join Table (Many-to-Many)

```sql
-- Flyway: V5__create_product_tag_table.sql
-- Equivalent to: a CF-ORM fieldtype="many-to-many" linktable / a DataMgr link table

CREATE TABLE product_tag (
    product_id BIGINT NOT NULL,
    tag_id     BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (product_id, tag_id),
    CONSTRAINT fk_pt_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_pt_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);
```

---

## Troubleshooting

### "Migration checksum mismatch"
A migration file was modified after being applied. Never edit applied migrations.
```bash
# Repair the schema history
mvn flyway:repair
```

### "Found non-empty schema without schema history table"
Database has existing tables but no Flyway tracking table.
```bash
# Baseline at the current state
mvn flyway:baseline
```

### "Validate failed: Detected applied migration not resolved locally"
A migration file was deleted. Don't delete applied migrations.
```bash
# Repair to remove missing entries
mvn flyway:repair
```

### Using environment-specific migrations
```yaml
# application-dev.yml
spring:
  flyway:
    locations: classpath:db/migration,classpath:db/dev-data

# application-prod.yml
spring:
  flyway:
    locations: classpath:db/migration
```
