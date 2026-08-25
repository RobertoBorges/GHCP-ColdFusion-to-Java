// =============================================================================
// JPA Entity Template
// Part of the PHP-to-Java Migration Framework
// =============================================================================
// This template shows how to structure a JPA entity migrated from Eloquent.
// Copy and customize for each entity in your application.
// =============================================================================

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

/**
 * Product entity — migrated from App\Models\Product (Eloquent).
 *
 * Implements Auditable and SoftDeletable interfaces for cross-cutting concerns.
 */
@Entity
@Table(
    name = "products",
    indexes = {
        @Index(name = "idx_product_slug", columnList = "slug", unique = true),
        @Index(name = "idx_product_category", columnList = "category_id"),
        @Index(name = "idx_product_active", columnList = "is_active")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_slug", columnNames = "slug")
    }
)
@SQLRestriction("deleted_at IS NULL") // Soft delete filter (replaces SoftDeletes trait)
public class Product implements Auditable, SoftDeletable {

    // ==========================================================================
    // Primary Key
    // Eloquent: protected $primaryKey = 'id'; (default, auto-increment)
    //
    // Generation strategies:
    //   IDENTITY  — MySQL AUTO_INCREMENT / PostgreSQL SERIAL (most common)
    //   SEQUENCE  — PostgreSQL sequences (best for batch inserts)
    //   UUID      — use UUID type for distributed systems
    // ==========================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================================================
    // Properties
    // Eloquent: protected $fillable = ['name', 'slug', 'description', ...]
    // In JPA, all fields are persisted by default. Use DTOs for input validation.
    // ==========================================================================

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255, unique = true)
    private String slug;

    /**
     * Description with large text support.
     * Eloquent: $casts not needed — use @Column or @Lob for large text.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Price with 2 decimal places.
     * Eloquent: 'price' => 'decimal:2'
     */
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    /**
     * Stock quantity.
     */
    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity = 0;

    /**
     * Active status.
     * Eloquent: 'is_active' => 'boolean'
     */
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /**
     * JSON metadata stored as string.
     * Eloquent: 'metadata' => 'array'
     *
     * For automatic JSON conversion, use an AttributeConverter:
     *   @Convert(converter = JsonMapConverter.class)
     *   private Map<String, Object> metadata;
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadataJson;

    /**
     * Enum field example.
     * Eloquent: 'status' => ProductStatus::class (PHP 8.1 enum cast)
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ProductStatus status = ProductStatus.DRAFT;

    // ==========================================================================
    // Foreign Keys
    // ==========================================================================

    /**
     * Category foreign key.
     * Eloquent: return $this->belongsTo(Category::class);
     * The @ManyToOne + @JoinColumn defines the FK relationship.
     */
    @Column(name = "category_id", nullable = false, insertable = false, updatable = false)
    private Long categoryId;

    /**
     * Optional user who created this product.
     */
    @Column(name = "created_by_user_id", insertable = false, updatable = false)
    private Long createdByUserId;

    // ==========================================================================
    // Timestamps (Auditable interface)
    // Eloquent: $timestamps = true (created_at, updated_at) — on by default
    // ==========================================================================

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==========================================================================
    // Soft Delete (SoftDeletable interface)
    // Eloquent: use SoftDeletes;
    // ==========================================================================

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ==========================================================================
    // Navigation Properties (Relationships)
    // ==========================================================================

    /**
     * Category this product belongs to.
     * Eloquent: return $this->belongsTo(Category::class);
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * User who created this product (optional).
     * Eloquent: return $this->belongsTo(User::class, 'created_by_user_id');
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    /**
     * Reviews for this product.
     * Eloquent: return $this->hasMany(Review::class);
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    /**
     * Order items containing this product.
     * Eloquent: return $this->hasMany(OrderItem::class);
     */
    @OneToMany(mappedBy = "product")
    private List<OrderItem> orderItems = new ArrayList<>();

    /**
     * Tags for this product (many-to-many).
     * Eloquent: return $this->belongsToMany(Tag::class);
     *
     * Note: Using Set<Tag> (not List) for proper equals/hashCode semantics
     * with many-to-many relationships.
     */
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "product_tag",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    /**
     * Images for this product (polymorphic).
     * Eloquent: return $this->morphMany(Image::class, 'imageable');
     *
     * In JPA, polymorphic relations use @Inheritance on the Image entity
     * or a discriminator column pattern.
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Image> images = new ArrayList<>();

    // ==========================================================================
    // Lifecycle Callbacks
    // Eloquent: static::creating(...), static::updating(...)
    // ==========================================================================

    @PrePersist
    public void onPrePersist() {
        if (this.slug == null && this.name != null) {
            this.slug = generateSlug(this.name);
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        // Add any pre-update logic here
    }

    // ==========================================================================
    // Computed Properties (replaces Accessors)
    // Eloquent: public function getFormattedPriceAttribute()
    // ==========================================================================

    @Transient
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

    // Convenience methods for managing tags (replaces belongsToMany attach/detach)
    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getProducts().add(this);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.getProducts().remove(this);
    }

    // Soft delete helpers
    @Override
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    @Override
    public void restore() {
        this.deletedAt = null;
    }

    @Override
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    // ==========================================================================
    // Mutator Logic (replaces Eloquent mutators)
    // ==========================================================================

    public void setName(String name) {
        this.name = name;
        this.slug = generateSlug(name);
    }

    private static String generateSlug(String input) {
        if (input == null) return null;
        return input.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    // ==========================================================================
    // equals/hashCode — based on business key (slug), NOT auto-generated ID
    //
    // IMPORTANT: Do not use @Id in equals/hashCode for JPA entities.
    // Generated IDs may be null before persistence, causing issues with
    // Sets and Maps. Use a natural/business key instead.
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

    @Override
    public String toString() {
        return "Product{id=%d, name='%s', slug='%s', price=%s}".formatted(id, name, slug, price);
    }

    // ==========================================================================
    // Getters and Setters
    // Tip: Use Lombok @Getter @Setter to reduce boilerplate in real projects
    // ==========================================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int qty) { this.stockQuantity = qty; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String json) { this.metadataJson = json; }
    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }
    public Long getCategoryId() { return categoryId; }
    public Long getCreatedByUserId() { return createdByUserId; }
    @Override public LocalDateTime getCreatedAt() { return createdAt; }
    @Override public LocalDateTime getUpdatedAt() { return updatedAt; }
    @Override public LocalDateTime getDeletedAt() { return deletedAt; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public User getCreatedByUser() { return createdByUser; }
    public void setCreatedByUser(User user) { this.createdByUser = user; }
    public List<Review> getReviews() { return reviews; }
    public List<OrderItem> getOrderItems() { return orderItems; }
    public Set<Tag> getTags() { return tags; }
    public List<Image> getImages() { return images; }
}

// =============================================================================
// Enum for Status field
// Eloquent: PHP 8.1 backed enums with $casts
// =============================================================================

enum ProductStatus {
    DRAFT,
    ACTIVE,
    ARCHIVED,
    DISCONTINUED
}

// =============================================================================
// Interfaces for Common Patterns
// =============================================================================

/**
 * Interface for entities with audit timestamps.
 * Eloquent: $timestamps = true
 */
interface Auditable {
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}

/**
 * Interface for soft-deletable entities.
 * Eloquent: use SoftDeletes;
 */
interface SoftDeletable {
    void softDelete();
    void restore();
    boolean isDeleted();
    LocalDateTime getDeletedAt();
}

// =============================================================================
// AttributeConverter for JSON columns
// Eloquent: 'metadata' => 'array' (automatic JSON cast)
// JPA: Use @Convert(converter = JsonMapConverter.class)
// =============================================================================

/*
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Map;

@Converter
public class JsonMapConverter implements AttributeConverter<Map<String, Object>, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        try {
            return attribute == null ? null : mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting map to JSON", e);
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ? null : mapper.readValue(dbData, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting JSON to map", e);
        }
    }
}
*/
