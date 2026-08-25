// =============================================================================
// Spring Data JPA Repository Template
// Part of the ColdFusion-to-Java Migration Framework
// =============================================================================
// This template shows how to create a Spring Data JPA repository.
// Replaces CFC finder methods, DataMgr queries, and hand-written <cfquery> DAOs.
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
 * Product repository — replaces CFC finder methods and <cfquery> data access.
 *
 * Spring Data JPA generates the implementation automatically.
 * CFML: queryExecute("SELECT ... FROM products WHERE ...") → productRepository.findByXxx()
 *
 * Extends:
 *   JpaRepository          — standard CRUD + pagination
 *   JpaSpecificationExecutor — dynamic query composition (replaces dynamic <cfquery> WHERE building)
 */
@Repository
public interface ProductRepository
        extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // ==========================================================================
    // Derived Query Methods (replaces CFC finder methods)
    //
    // Spring Data JPA generates queries from method names automatically.
    // CFML: <cfquery>SELECT * FROM products WHERE is_active = 1</cfquery>
    // Spring:   productRepository.findByActiveTrue()
    // ==========================================================================

    // getActiveProducts() → findByActiveTrue
    List<Product> findByActiveTrue();

    // getByCategory(id) → findByCategoryId
    List<Product> findByCategoryId(Long categoryId);

    // getByPriceRange(min,max) → findByPriceBetween
    List<Product> findByPriceBetween(BigDecimal min, BigDecimal max);

    // CFML: SELECT * FROM products WHERE slug = <cfqueryparam value="#slug#"> (first row)
    Optional<Product> findBySlug(String slug);

    // CFML: SELECT * FROM products WHERE status = <..> AND category_id = <..>
    List<Product> findByStatusAndCategoryId(ProductStatus status, Long categoryId);

    // CFML: SELECT * FROM products WHERE name LIKE <cfqueryparam value="%#query#%">
    List<Product> findByNameContainingIgnoreCase(String name);

    // Combined finders via method name composition:
    // CFML: getActiveProductsByCategory(id)
    List<Product> findByActiveTrueAndCategoryId(Long categoryId);

    // CFML: SELECT * FROM products WHERE created_at > <cfqueryparam value="#date#">
    List<Product> findByCreatedAtAfter(LocalDateTime date);

    // CFML: SELECT * FROM products WHERE price >= <..> AND price <= <..> AND is_active = 1
    List<Product> findByActiveTrueAndPriceBetween(BigDecimal min, BigDecimal max);

    // Search with multiple filters
    List<Product> findByNameContainingIgnoreCaseAndCategoryId(String name, Long categoryId);

    List<Product> findByNameContainingIgnoreCaseAndCategoryIdAndPriceBetween(
            String name, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice);

    // ==========================================================================
    // Pagination and Sorting
    // CFML: <cfquery maxrows=...> + startRow, or SQL LIMIT/OFFSET → Pageable
    // ==========================================================================

    // CFML: paged query of active products
    Page<Product> findByActiveTrue(Pageable pageable);

    // CFML: SELECT * FROM products WHERE category_id = <..> ORDER BY price ASC
    List<Product> findByCategoryId(Long categoryId, Sort sort);

    // CFML: paged query filtered by category
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    // ==========================================================================
    // JPQL Queries (@Query)
    // For queries too complex for derived method names.
    // CFML: dynamic <cfquery> blocks assembled with <cfif>/string concatenation
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

    // CFML: WHERE name LIKE <..> OR description LIKE <..>
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
    // CFML: raw <cfquery> SQL against the datasource
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
    // CFML: <cfquery>UPDATE products SET is_active = 0 WHERE category_id = <..></cfquery>
    // ==========================================================================

    @Modifying
    @Query("UPDATE Product p SET p.active = false WHERE p.category.id = :categoryId")
    int deactivateByCategory(@Param("categoryId") Long categoryId);

    @Modifying
    @Query("UPDATE Product p SET p.status = :status WHERE p.id IN :ids")
    int updateStatusForIds(@Param("ids") List<Long> ids, @Param("status") ProductStatus status);

    // Soft delete via JPQL (replaces UPDATE products SET deleted_at = <now> in CFML)
    @Modifying
    @Query("UPDATE Product p SET p.deletedAt = :now WHERE p.id = :id")
    int softDeleteById(@Param("id") Long id, @Param("now") LocalDateTime now);

    // Restore soft-deleted (CFML: UPDATE products SET deleted_at = NULL WHERE id = <..>)
    @Modifying
    @Query("UPDATE Product p SET p.deletedAt = null WHERE p.id = :id")
    int restoreById(@Param("id") Long id);

    // ==========================================================================
    // @EntityGraph — eager loading (replaces separate <cfquery> lookups per row)
    // Avoids N+1 queries by joining related entities.
    // ==========================================================================

    @EntityGraph(attributePaths = {"category", "tags"})
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithCategoryAndTags(@Param("id") Long id);

    @EntityGraph(attributePaths = {"category"})
    List<Product> findByActiveTrueOrderByCreatedAtDesc();

    // ==========================================================================
    // Projections (replaces SELECT id, name, price in a <cfquery>)
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
    // CFML: SELECT COUNT(*) FROM products WHERE category_id = <..>
    // CFML: SELECT 1 FROM products WHERE slug = <..> (recordCount > 0)
    // ==========================================================================

    long countByCategoryId(Long categoryId);

    boolean existsBySlug(String slug);

    boolean existsByNameIgnoreCase(String name);
}

// =============================================================================
// Specifications for Dynamic Queries (replaces dynamic <cfquery> WHERE building)
// =============================================================================
//
// Use when you need to compose queries dynamically at runtime,
// similar to a CFC that appends WHERE clauses with <cfif> around a <cfquery>.
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

    // CFML: <cfif active> AND is_active = 1 </cfif>
    public static Specification<Product> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    // CFML: <cfif structKeyExists(filters,"category")> AND category_id = <..> </cfif>
    public static Specification<Product> inCategory(Long categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    // CFML: <cfif min neq "" and max neq ""> AND price BETWEEN <..> AND <..> </cfif>
    public static Specification<Product> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> cb.between(root.get("price"), min, max);
    }

    // CFML: AND name LIKE <cfqueryparam value="%#name#%">
    public static Specification<Product> nameContains(String name) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    // CFML: AND created_at > <cfqueryparam value="#date#">
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
