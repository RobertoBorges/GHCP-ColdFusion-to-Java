// =============================================================================
// ColdFusion (CFML) to Java JPA Entity Conversion Example
// Part of the ColdFusion-to-Java Migration Framework
// =============================================================================

// -----------------------------------------------------------------------------
// BEFORE: ColdFusion CF-ORM persistent component (cfcs/Product.cfc)
// CF-ORM persistent CFCs map most directly to JPA entities. Apps using DataMgr
// or hand-written <cfquery> DAOs convert the same way — the columns and
// relationships below become @Column and @ManyToOne/@OneToMany mappings.
// -----------------------------------------------------------------------------
/*
component persistent="true" table="products" output="false" {

    // Primary key
    property name="id" fieldtype="id" generator="identity";

    // Columns
    property name="name"        ormtype="string"    length="255" notnull="true";
    property name="slug"        ormtype="string"    length="255" unique="true";
    property name="description" ormtype="text";
    property name="price"       ormtype="big_decimal" notnull="true";
    property name="active"      ormtype="boolean"   column="is_active" default="true";
    property name="stockQuantity" ormtype="integer" column="stock_quantity";
    property name="metadata"    ormtype="text"; // JSON stored as text

    // Timestamps
    property name="createdAt" ormtype="timestamp" column="created_at";
    property name="updatedAt" ormtype="timestamp" column="updated_at";
    property name="deletedAt" ormtype="timestamp" column="deleted_at"; // soft delete flag

    // Relationships
    property name="category" fieldtype="many-to-one" cfc="Category" fkcolumn="category_id";
    property name="reviews"  fieldtype="one-to-many" cfc="Review" fkcolumn="product_id";
    property name="tags"     fieldtype="many-to-many" cfc="Tag"
             linktable="product_tag" fkcolumn="product_id" inversejoincolumn="tag_id";
    property name="images"   fieldtype="one-to-many" cfc="Image" fkcolumn="product_id";

    // "Accessor": formatted price (a computed getter / UDF)
    public string function getFormattedPrice() {
        return dollarFormat(variables.price);
    }

    // "Mutator": setting the name also derives the slug
    public void function setName(required string name) {
        variables.name = arguments.name;
        variables.slug = reReplace(lCase(trim(arguments.name)), "[^a-z0-9]+", "-", "all");
    }

    // Business logic
    public boolean function isAvailable() {
        return variables.active && variables.stockQuantity gt 0;
    }

    public numeric function applyDiscount(required numeric percentage) {
        return variables.price * (1 - arguments.percentage / 100);
    }
}
*/

// -----------------------------------------------------------------------------
// AFTER: Java 21 JPA Entity (entity/Product.java)
// -----------------------------------------------------------------------------

package com.example.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_slug", columnList = "slug", unique = true),
    @Index(name = "idx_product_category", columnList = "category_id"),
    @Index(name = "idx_product_active", columnList = "is_active")
})
@SQLRestriction("deleted_at IS NULL") // Soft delete filter (replaces the deletedAt convention)
public class Product {

    // ==========================================================================
    // Primary Key
    // CF-ORM: property name="id" fieldtype="id" generator="identity";
    // ==========================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================================================
    // Properties (replaces the cfproperty column definitions)
    // ==========================================================================

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ormtype="big_decimal" → BigDecimal with column precision
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    // ormtype="boolean" column="is_active" → boolean type maps directly
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    private int stockQuantity;

    // metadata stored as JSON text → convert in getter/setter
    @Column(columnDefinition = "TEXT")
    private String metadataJson;

    // ==========================================================================
    // Timestamps (replaces ormtype="timestamp" columns / DataMgr audit columns)
    // ==========================================================================

    // created_at → Hibernate @CreationTimestamp
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonIgnore // exclude from JSON serialization
    private LocalDateTime createdAt;

    // updated_at → Hibernate @UpdateTimestamp
    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonIgnore
    private LocalDateTime updatedAt;

    // ==========================================================================
    // Soft Delete (replaces the deletedAt timestamp convention)
    // Combined with @SQLRestriction on the class
    // ==========================================================================

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ==========================================================================
    // Relationships (replaces CF-ORM fieldtype relationships)
    // ==========================================================================

    // fieldtype="many-to-one" → @ManyToOne
    // CF-ORM: property name="category" fieldtype="many-to-one" cfc="Category";
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // fieldtype="one-to-many" → @OneToMany
    // CF-ORM: property name="reviews" fieldtype="one-to-many" cfc="Review";
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    // fieldtype="many-to-many" (linktable) → @ManyToMany with @JoinTable
    // CF-ORM: property name="tags" fieldtype="many-to-many" cfc="Tag" linktable="product_tag";
    @ManyToMany
    @JoinTable(
        name = "product_tag",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    // CF-ORM has no polymorphic relationship; model a plain one-to-many
    // (or @Inheritance on Image) for what a generic "imageable" table would hold.
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Image> images = new ArrayList<>();

    // ==========================================================================
    // Computed Properties (replaces computed getters / UDFs)
    // CF-ORM: public string function getFormattedPrice()
    // ==========================================================================

    @Transient // Not persisted — a computed accessor
    public String getFormattedPrice() {
        return NumberFormat.getCurrencyInstance(Locale.US).format(price);
    }

    @Transient
    public boolean isInStock() {
        return stockQuantity > 0;
    }

    @Transient
    public boolean isAvailable() {
        return active && isInStock();
    }

    // ==========================================================================
    // Mutator Logic (replaces the setName() that also derived the slug)
    // In JPA, put mutator logic directly in the setter
    // ==========================================================================

    public void setName(String name) {
        this.name = name;
        // Auto-generate slug when name is set (replaces reReplace()/lCase() in the CFC setter)
        this.slug = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    // ==========================================================================
    // Business Logic Methods
    // ==========================================================================

    public BigDecimal applyDiscount(BigDecimal percentage) {
        if (percentage.compareTo(BigDecimal.ZERO) < 0
                || percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
        return price.multiply(BigDecimal.ONE.subtract(percentage.divide(BigDecimal.valueOf(100))));
    }

    public boolean canFulfill(int quantity) {
        return active && stockQuantity >= quantity;
    }

    public void reduceStock(int quantity) {
        if (quantity > stockQuantity) {
            throw new IllegalStateException("Insufficient stock");
        }
        stockQuantity -= quantity;
    }

    // Soft delete helper
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.deletedAt = null;
    }

    // ==========================================================================
    // equals/hashCode — use business key (slug), not generated ID
    // ==========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product other)) return false;
        return slug != null && slug.equals(other.slug);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slug);
    }

    // ==========================================================================
    // Standard getters and setters (omitted for brevity — use Lombok @Getter/@Setter
    // or IDE generation in real projects)
    // ==========================================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public List<Review> getReviews() { return reviews; }
    public Set<Tag> getTags() { return tags; }
    public List<Image> getImages() { return images; }
}

// -----------------------------------------------------------------------------
// Spring Data JPA Repository (replaces CFC finder methods / DataMgr queries)
// CFC finders and <cfquery> lookups become repository query methods
// repository/ProductRepository.java
// -----------------------------------------------------------------------------

/*
package com.example.app.repository;

import com.example.app.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // "getActiveProducts()" finder → findByActiveTrue()
    List<Product> findByActiveTrue();

    // "getByCategory(id)" finder → findByCategoryId()
    List<Product> findByCategoryId(Long categoryId);

    // "getByPriceRange(min,max)" finder → findByPriceBetween()
    List<Product> findByPriceBetween(BigDecimal min, BigDecimal max);

    // Combined finders → compose in method name
    List<Product> findByActiveTrueAndCategoryId(Long categoryId);

    // Complex queries → @Query with JPQL
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.price BETWEEN :min AND :max")
    List<Product> findActivePriceRange(BigDecimal min, BigDecimal max);

    // Search by name (like "%query%")
    List<Product> findByNameContainingIgnoreCase(String name);

    // Search with filters
    List<Product> findByNameContainingIgnoreCaseAndCategoryId(String name, Long categoryId);

    List<Product> findByNameContainingIgnoreCaseAndCategoryIdAndPriceBetween(
        String name, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice);
}
*/

// -----------------------------------------------------------------------------
// KEY CONVERSION PATTERNS:
// -----------------------------------------------------------------------------
// 1.  cfproperty columns → @Column fields; use DTOs (records) for input binding
// 2.  ormtype → proper Java types (BigDecimal, boolean, LocalDateTime)
// 3.  Hidden/internal columns → @JsonIgnore to exclude from JSON serialization
// 4.  fieldtype="many-to-one" → @ManyToOne with @JoinColumn
// 5.  fieldtype="one-to-many" → @OneToMany(mappedBy = "field")
// 6.  fieldtype="many-to-many" (linktable) → @ManyToMany with @JoinTable
// 7.  Polymorphic tables (no CF-ORM equivalent) → @Inheritance strategy or discriminator
// 8.  deletedAt convention → @SQLRestriction("deleted_at IS NULL") + deletedAt field
// 9.  ormtype="timestamp" audit columns → @CreationTimestamp / @UpdateTimestamp (Hibernate)
//     or @CreatedDate / @LastModifiedDate (Spring Data Auditing)
// 10. CFC finder methods / DataMgr queries → Spring Data JPA derived query methods (findByXxx)
// 11. Computed getters / UDFs → @Transient getter methods
// 12. CFC setter logic → logic inside Java setter methods
// 13. equals/hashCode → Based on business key, not auto-generated ID
// 14. Use FetchType.LAZY for relationships to avoid N+1 queries
