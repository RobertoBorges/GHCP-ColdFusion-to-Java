// =============================================================================
// Spring Data JPA Repository Template
// Part of the PHP-to-Java Migration Framework
// =============================================================================
// This template shows how to create a Spring Data JPA repository.
// Replaces Eloquent query methods, scopes, and repository patterns.
// =============================================================================

package com.example.app.repository;

import com.example.app.entity.Product;
import com.example.app.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Product repository — replaces Eloquent model query methods and scopes.
 *
 * Spring Data JPA generates the implementation automatically.
 * Eloquent: Product::where(...)->get() → productRepository.findByXxx()
 *
 * Extends:
 *   JpaRepository          — standard CRUD + pagination
 *   JpaSpecificationExecutor — dynamic query composition (replaces scope chaining)
 */
@Repository
public interface ProductRepository
        extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // ==========================================================================
    // Derived Query Methods (replaces Eloquent scopes)
    //
    // Spring Data JPA generates queries from method names automatically.
    // Eloquent: Product::where('is_active', true)->get()
    // Spring:   productRepository.findByActiveTrue()
    // ==========================================================================

    // scopeActive → findByActiveTrue
    List<Product> findByActiveTrue();

    // scopeInCategory → findByCategoryId
    List<Product> findByCategoryId(Long categoryId);

    // scopePriceRange → findByPriceBetween
    List<Product> findByPriceBetween(BigDecimal min, BigDecimal max);

    // Eloquent: Product::where('slug', $slug)->first()
    Optional<Product> findBySlug(String slug);

    // Eloquent: Product::where('status', 'active')->where('category_id', $id)->get()
    List<Product> findByStatusAndCategoryId(ProductStatus status, Long categoryId);

    // Eloquent: Product::where('name', 'like', "%$query%")->get()
    List<Product> findByNameContainingIgnoreCase(String name);

    // Combined scopes via method name composition:
    // Eloquent: Product::active()->inCategory($id)->get()
    List<Product> findByActiveTrueAndCategoryId(Long categoryId);

    // Eloquent: Product::where('created_at', '>', $date)->get()
    List<Product> findByCreatedAtAfter(LocalDateTime date);

    // Eloquent: Product::where('price', '>=', $min)->where('price', '<=', $max)->where('is_active', true)->get()
    List<Product> findByActiveTrueAndPriceBetween(BigDecimal min, BigDecimal max);

    // Search with multiple filters
    List<Product> findByNameContainingIgnoreCaseAndCategoryId(String name, Long categoryId);

    List<Product> findByNameContainingIgnoreCaseAndCategoryIdAndPriceBetween(
            String name, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice);

    // ==========================================================================
    // Pagination and Sorting
    // Eloquent: Product::paginate(15) → productRepository.findAll(Pageable)
    // ==========================================================================

    // Eloquent: Product::where('is_active', true)->paginate(10)
    Page<Product> findByActiveTrue(Pageable pageable);

    // Eloquent: Product::where('category_id', $id)->orderBy('price', 'asc')->get()
    List<Product> findByCategoryId(Long categoryId, Sort sort);

    // Eloquent: Product::where('category_id', $id)->paginate(10)
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    // ==========================================================================
    // JPQL Queries (@Query)
    // For queries too complex for derived method names.
    // Eloquent: Product::whereRaw(...) or complex query builder chains
    // ==========================================================================

    // Complex filter with JPQL
    @Query("""
        SELECT p FROM Product p
        WHERE p.active = true
          AND p.price BETWEEN :minPrice AND :maxPrice
          AND p.category.id = :categoryId
        ORDER BY p.price ASC
        """)
    List<Product> findActiveProductsInCategoryAndPriceRange(
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);

    // Eloquent: Product::where('name', 'like', "%$query%")->orWhere('description', 'like', "%$query%")->get()
    @Query("""
        SELECT p FROM Product p
        WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))
        """)
    List<Product> searchByNameOrDescription(@Param("search") String search);

    // JPQL with pagination
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.status = :status")
    Page<Product> findActiveByStatus(@Param("status") ProductStatus status, Pageable pageable);

    // ==========================================================================
    // Native SQL Queries
    // Eloquent: DB::select('SELECT ... FROM products WHERE ...')
    // Use sparingly — prefer JPQL for portability.
    // ==========================================================================

    @Query(value = """
        SELECT p.* FROM products p
        INNER JOIN product_tag pt ON p.id = pt.product_id
        WHERE pt.tag_id = :tagId AND p.deleted_at IS NULL
        ORDER BY p.created_at DESC
        """, nativeQuery = true)
    List<Product> findByTagId(@Param("tagId") Long tagId);

    // Native query with pagination
    @Query(value = "SELECT * FROM products WHERE is_active = true AND deleted_at IS NULL",
           countQuery = "SELECT count(*) FROM products WHERE is_active = true AND deleted_at IS NULL",
           nativeQuery = true)
    Page<Product> findAllActiveNative(Pageable pageable);

    // ==========================================================================
    // @Modifying for UPDATE/DELETE operations
    // Eloquent: Product::where('category_id', $id)->update(['is_active' => false])
    // ==========================================================================

    @Modifying
    @Query("UPDATE Product p SET p.active = false WHERE p.category.id = :categoryId")
    int deactivateByCategory(@Param("categoryId") Long categoryId);

    @Modifying
    @Query("UPDATE Product p SET p.status = :status WHERE p.id IN :ids")
    int updateStatusForIds(@Param("ids") List<Long> ids, @Param("status") ProductStatus status);

    // Soft delete via JPQL (replaces Eloquent $model->delete() with SoftDeletes)
    @Modifying
    @Query("UPDATE Product p SET p.deletedAt = :now WHERE p.id = :id")
    int softDeleteById(@Param("id") Long id, @Param("now") LocalDateTime now);

    // Restore soft-deleted (Eloquent: $model->restore())
    @Modifying
    @Query("UPDATE Product p SET p.deletedAt = null WHERE p.id = :id")
    int restoreById(@Param("id") Long id);

    // ==========================================================================
    // @EntityGraph — eager loading (replaces Eloquent ::with(['category', 'tags']))
    // Avoids N+1 queries by joining related entities.
    // ==========================================================================

    @EntityGraph(attributePaths = {"category", "tags"})
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithCategoryAndTags(@Param("id") Long id);

    @EntityGraph(attributePaths = {"category"})
    List<Product> findByActiveTrueOrderByCreatedAtDesc();

    // ==========================================================================
    // Projections (replaces Eloquent ::select('id', 'name', 'price'))
    // Returns only the fields you need, improving performance.
    // ==========================================================================

    // Interface-based projection
    interface ProductSummary {
        Long getId();
        String getName();
        BigDecimal getPrice();
        String getSlug();
    }

    List<ProductSummary> findByActiveTrueOrderByNameAsc();

    // ==========================================================================
    // Count and Exists
    // Eloquent: Product::where('category_id', $id)->count()
    // Eloquent: Product::where('slug', $slug)->exists()
    // ==========================================================================

    long countByCategoryId(Long categoryId);

    boolean existsBySlug(String slug);

    boolean existsByNameIgnoreCase(String name);
}

// =============================================================================
// Specifications for Dynamic Queries (replaces Eloquent scope composition)
// =============================================================================
//
// Use when you need to compose queries dynamically at runtime,
// similar to chaining Eloquent scopes: Product::active()->inCategory($id)->get()
//
// Usage in service:
//   Specification<Product> spec = ProductSpecifications.isActive()
//       .and(ProductSpecifications.inCategory(categoryId))
//       .and(ProductSpecifications.priceBetween(min, max));
//   List<Product> products = productRepository.findAll(spec);
//
// =============================================================================

/*
package com.example.app.repository;

import com.example.app.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class ProductSpecifications {

    private ProductSpecifications() {} // utility class

    // Eloquent: scopeActive
    public static Specification<Product> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    // Eloquent: scopeInCategory
    public static Specification<Product> inCategory(Long categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    // Eloquent: scopePriceRange
    public static Specification<Product> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> cb.between(root.get("price"), min, max);
    }

    // Eloquent: where('name', 'like', "%$query%")
    public static Specification<Product> nameContains(String name) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    // Eloquent: where('created_at', '>', $date)
    public static Specification<Product> createdAfter(java.time.LocalDateTime date) {
        return (root, query, cb) -> cb.greaterThan(root.get("createdAt"), date);
    }
}
*/

// =============================================================================
// Custom Repository Implementation (for complex queries not easily expressed
// in method names or JPQL)
// =============================================================================

/*
package com.example.app.repository;

import com.example.app.entity.Product;
import java.util.List;

// Custom interface
public interface ProductRepositoryCustom {
    List<Product> findWithComplexFilters(ProductSearchCriteria criteria);
}

// Implementation — must be named {RepositoryName}Impl
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;

public class ProductRepositoryImpl implements ProductRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Product> findWithComplexFilters(ProductSearchCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> cq = cb.createQuery(Product.class);
        Root<Product> root = cq.from(Product.class);

        List<Predicate> predicates = new java.util.ArrayList<>();

        if (criteria.name() != null) {
            predicates.add(cb.like(cb.lower(root.get("name")),
                "%" + criteria.name().toLowerCase() + "%"));
        }
        if (criteria.categoryId() != null) {
            predicates.add(cb.equal(root.get("category").get("id"), criteria.categoryId()));
        }
        if (criteria.minPrice() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("price"), criteria.minPrice()));
        }
        if (criteria.maxPrice() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("price"), criteria.maxPrice()));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("name")));

        return entityManager.createQuery(cq).getResultList();
    }
}

// Then extend in the main repository:
// public interface ProductRepository extends JpaRepository<Product, Long>,
//     JpaSpecificationExecutor<Product>, ProductRepositoryCustom { ... }
*/
