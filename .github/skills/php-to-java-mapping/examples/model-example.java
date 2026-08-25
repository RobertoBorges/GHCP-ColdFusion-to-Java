// =============================================================================
// PHP Eloquent to Java JPA Entity Conversion Example
// Part of the PHP-to-Java Migration Framework
// =============================================================================

// -----------------------------------------------------------------------------
// BEFORE: PHP Laravel Eloquent Model (app/Models/Product.php)
// -----------------------------------------------------------------------------
/*
<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\SoftDeletes;

class Product extends Model
{
    use HasFactory, SoftDeletes;

    protected $fillable = [
        'name',
        'slug',
        'description',
        'price',
        'category_id',
        'is_active',
    ];

    protected $casts = [
        'price' => 'decimal:2',
        'is_active' => 'boolean',
        'metadata' => 'array',
    ];

    protected $hidden = [
        'created_at',
        'updated_at',
    ];

    // Relationships
    public function category()
    {
        return $this->belongsTo(Category::class);
    }

    public function tags()
    {
        return $this->belongsToMany(Tag::class);
    }

    public function reviews()
    {
        return $this->hasMany(Review::class);
    }

    public function images()
    {
        return $this->morphMany(Image::class, 'imageable');
    }

    // Scopes
    public function scopeActive($query)
    {
        return $query->where('is_active', true);
    }

    public function scopeInCategory($query, $categoryId)
    {
        return $query->where('category_id', $categoryId);
    }

    public function scopePriceRange($query, $min, $max)
    {
        return $query->whereBetween('price', [$min, $max]);
    }

    // Accessors
    public function getFormattedPriceAttribute()
    {
        return '$' . number_format($this->price, 2);
    }

    // Mutators
    public function setNameAttribute($value)
    {
        $this->attributes['name'] = $value;
        $this->attributes['slug'] = Str::slug($value);
    }

    // Business Logic
    public function isAvailable()
    {
        return $this->is_active && $this->stock_quantity > 0;
    }

    public function applyDiscount($percentage)
    {
        return $this->price * (1 - $percentage / 100);
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
@SQLRestriction("deleted_at IS NULL") // Soft delete filter (replaces SoftDeletes trait)
public class Product {

    // ==========================================================================
    // Primary Key
    // Eloquent: protected $primaryKey = 'id'; (auto-increment by default)
    // ==========================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================================================
    // Properties (replaces $fillable and $casts)
    // ==========================================================================

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    // 'price' => 'decimal:2' → BigDecimal with column precision
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    // 'is_active' => 'boolean' → boolean type maps directly
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    private int stockQuantity;

    // 'metadata' => 'array' → store as JSON string, convert in getter/setter
    @Column(columnDefinition = "TEXT")
    private String metadataJson;

    // ==========================================================================
    // Timestamps (replaces $timestamps = true via Hibernate annotations)
    // ==========================================================================

    // Eloquent: created_at → Hibernate @CreationTimestamp
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonIgnore // replaces $hidden for JSON serialization
    private LocalDateTime createdAt;

    // Eloquent: updated_at → Hibernate @UpdateTimestamp
    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonIgnore
    private LocalDateTime updatedAt;

    // ==========================================================================
    // Soft Delete (replaces use SoftDeletes)
    // Combined with @SQLRestriction on the class
    // ==========================================================================

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ==========================================================================
    // Relationships (replaces Eloquent relationship methods)
    // ==========================================================================

    // belongsTo → @ManyToOne
    // Eloquent: return $this->belongsTo(Category::class);
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // hasMany → @OneToMany
    // Eloquent: return $this->hasMany(Review::class);
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    // belongsToMany → @ManyToMany with @JoinTable
    // Eloquent: return $this->belongsToMany(Tag::class);
    @ManyToMany
    @JoinTable(
        name = "product_tag",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    // morphMany → @OneToMany with discriminator (or @Inheritance on Image)
    // Eloquent: return $this->morphMany(Image::class, 'imageable');
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Image> images = new ArrayList<>();

    // ==========================================================================
    // Computed Properties (replaces Accessors)
    // Eloquent: public function getFormattedPriceAttribute()
    // ==========================================================================

    @Transient // Not persisted — equivalent to [NotMapped] or a computed accessor
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
    // Mutator Logic (replaces setNameAttribute)
    // In JPA, put mutator logic directly in the setter
    // ==========================================================================

    public void setName(String name) {
        this.name = name;
        // Auto-generate slug when name is set (replaces Str::slug in mutator)
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
// Spring Data JPA Repository (replaces Eloquent scopes)
// Scopes become repository query methods
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

    // scopeActive → findByActiveTrue()
    List<Product> findByActiveTrue();

    // scopeInCategory → findByCategoryId()
    List<Product> findByCategoryId(Long categoryId);

    // scopePriceRange → findByPriceBetween()
    List<Product> findByPriceBetween(BigDecimal min, BigDecimal max);

    // Combined scopes → compose in method name
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
// 1.  $fillable → Not needed; use DTOs (records) for input, entity for persistence
// 2.  $casts → Use proper Java types (BigDecimal, boolean, LocalDateTime)
// 3.  $hidden → @JsonIgnore on fields to exclude from JSON serialization
// 4.  belongsTo → @ManyToOne with @JoinColumn
// 5.  hasMany → @OneToMany(mappedBy = "field")
// 6.  belongsToMany → @ManyToMany with @JoinTable
// 7.  morphMany → @OneToMany with @Inheritance(strategy) on parent, or discriminator
// 8.  SoftDeletes → @SQLRestriction("deleted_at IS NULL") + deletedAt field
// 9.  Timestamps → @CreationTimestamp / @UpdateTimestamp (Hibernate)
//     or @CreatedDate / @LastModifiedDate (Spring Data Auditing)
// 10. Scopes → Spring Data JPA derived query methods (findByXxx)
// 11. Accessors → @Transient getter methods
// 12. Mutators → Logic inside setter methods
// 13. equals/hashCode → Based on business key, not auto-generated ID
// 14. Use FetchType.LAZY for relationships to avoid N+1 queries
