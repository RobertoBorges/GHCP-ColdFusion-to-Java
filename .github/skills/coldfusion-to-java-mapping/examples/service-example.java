// =============================================================================
// ColdFusion (CFML) to Java Service Conversion Example
// Part of the ColdFusion-to-Java Migration Framework
// =============================================================================

// -----------------------------------------------------------------------------
// BEFORE: ColdFusion service CFC (cfcs/ProductService.cfc)
// Typically instantiated once and stored in the application scope:
//   application.productService = createObject("component","cfcs.ProductService").init()
// -----------------------------------------------------------------------------
/*
component displayname="ProductService" output="false" {

    // init() is the CFC "constructor"; dependencies are wired manually
    public function init(productDao) {
        variables.productDao = arguments.productDao;
        return this;
    }

    public array function getAllProducts() {
        // CF caching: reuse a cached result if present, else query and cache it
        var cached = cacheGet("products.all");
        if (!isNull(cached)) {
            return cached;
        }
        writeLog(file="app", text="Fetching all products from database");
        var products = variables.productDao.all();
        cachePut("products.all", products, createTimeSpan(0,1,0,0)); // 1 hour
        return products;
    }

    public any function getProductById(required numeric id) {
        var cacheKey = "products." & arguments.id;
        var cached = cacheGet(cacheKey);
        if (!isNull(cached)) {
            return cached;
        }
        var product = variables.productDao.find(arguments.id);
        cachePut(cacheKey, product, createTimeSpan(0,1,0,0));
        return product;
    }

    public any function createProduct(required struct data) {
        var product = variables.productDao.create(arguments.data);
        cacheRemove("products.all");
        writeLog(file="app", text="Product created, id=" & product.id);
        return product;
    }

    public any function updateProduct(required numeric id, required struct data) {
        var product = variables.productDao.update(arguments.id, arguments.data);
        cacheRemove("products.all");
        cacheRemove("products." & arguments.id);
        writeLog(file="app", text="Product updated, id=" & arguments.id);
        return product;
    }

    public void function deleteProduct(required numeric id) {
        variables.productDao.delete(arguments.id);
        cacheRemove("products.all");
        cacheRemove("products." & arguments.id);
        writeLog(file="app", text="Product deleted, id=" & arguments.id);
    }

    // Dynamic search built up with <cfquery> + <cfqueryparam>
    public query function searchProducts(required string q, struct filters = {}) {
        var sql = "SELECT * FROM products WHERE name LIKE :q";
        var params = { q = { value = "%" & arguments.q & "%", cfsqltype = "cf_sql_varchar" } };

        if (structKeyExists(arguments.filters, "category")) {
            sql &= " AND category_id = :category";
            params.category = { value = arguments.filters.category, cfsqltype = "cf_sql_integer" };
        }
        if (structKeyExists(arguments.filters, "min_price")) {
            sql &= " AND price >= :minPrice";
            params.minPrice = { value = arguments.filters.min_price, cfsqltype = "cf_sql_decimal" };
        }
        if (structKeyExists(arguments.filters, "max_price")) {
            sql &= " AND price <= :maxPrice";
            params.maxPrice = { value = arguments.filters.max_price, cfsqltype = "cf_sql_decimal" };
        }
        return queryExecute(sql, params, { datasource = "appDS" });
    }
}
*/

// -----------------------------------------------------------------------------
// AFTER: Spring Boot Service (service/ProductService.java)
// -----------------------------------------------------------------------------

package com.example.app.service;

import com.example.app.dto.CreateProductRequest;
import com.example.app.dto.ProductSearchFilters;
import com.example.app.dto.UpdateProductRequest;
import com.example.app.entity.Product;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// @Service registers this class in the Spring ApplicationContext
// (replaces the application-scope singleton CFC pattern)
@Service
@Transactional(readOnly = true) // Default read-only transactions for query methods
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    // Constructor injection replaces init(productDao) + manual application-scope wiring;
    // Spring auto-wires single-constructor beans.
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // cacheGet/cachePut("products.all") → @Cacheable
    @Cacheable(value = "products", key = "'all'")
    public List<Product> getAllProducts() {
        log.info("Fetching all products from database");
        return productRepository.findAll();
    }

    // cacheGet/cachePut("products." & id) → @Cacheable with dynamic key
    @Cacheable(value = "products", key = "#id")
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    // cacheRemove("products.all") → @CacheEvict
    @Transactional // Override read-only for write operations
    @CacheEvict(value = "products", key = "'all'")
    public Product createProduct(CreateProductRequest request) {
        var product = new Product();
        product.setName(request.name());
        product.setPrice(request.price());
        product.setDescription(request.description());
        product.setActive(true);

        product = productRepository.save(product);
        log.info("Product created: {}", product.getId());

        return product;
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", key = "'all'"),
        @CacheEvict(value = "products", key = "#id")
    })
    public Product updateProduct(Long id, UpdateProductRequest request) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        product.setName(request.name());
        product.setPrice(request.price());
        product.setDescription(request.description());

        product = productRepository.save(product);
        log.info("Product updated: {}", id);

        return product;
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", key = "'all'"),
        @CacheEvict(value = "products", key = "#id")
    })
    public void deleteProduct(Long id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        productRepository.delete(product);
        log.info("Product deleted: {}", id);
    }

    // Replaces the dynamic <cfquery>/<cfqueryparam> search with repository query methods
    public List<Product> searchProducts(String query, ProductSearchFilters filters) {
        if (filters != null && filters.categoryId() != null
                && filters.minPrice() != null && filters.maxPrice() != null) {
            return productRepository.findByNameContainingIgnoreCaseAndCategoryIdAndPriceBetween(
                    query, filters.categoryId(), filters.minPrice(), filters.maxPrice());
        }
        if (filters != null && filters.categoryId() != null) {
            return productRepository.findByNameContainingIgnoreCaseAndCategoryId(
                    query, filters.categoryId());
        }
        return productRepository.findByNameContainingIgnoreCase(query);
    }
}

// -----------------------------------------------------------------------------
// DTO: Search Filters (dto/ProductSearchFilters.java)
// Java record replaces a CFML struct for typed, immutable data
// -----------------------------------------------------------------------------

/*
package com.example.app.dto;

import java.math.BigDecimal;

public record ProductSearchFilters(
    Long categoryId,
    BigDecimal minPrice,
    BigDecimal maxPrice
) {}
*/

// -----------------------------------------------------------------------------
// Custom Exception (exception/ResourceNotFoundException.java)
// Replaces <cfheader statuscode="404"> / <cfthrow>
// -----------------------------------------------------------------------------

/*
package com.example.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " not found with id: " + id);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
*/

// -----------------------------------------------------------------------------
// Spring Cache Configuration (config/CacheConfig.java)
// Replaces CF <cfcache> / CF Administrator cache settings
// -----------------------------------------------------------------------------

/*
package com.example.app.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching // Enables @Cacheable, @CacheEvict annotations
public class CacheConfig {
    // Spring Boot auto-configures a ConcurrentMapCacheManager by default.
    // For Redis, add spring-boot-starter-data-redis and configure in application.yml:
    //   spring.cache.type: redis
    //   spring.data.redis.host: localhost
}
*/

// -----------------------------------------------------------------------------
// Register in Spring (automatic — no manual registration needed)
// Unlike a CFC placed in the application scope by hand, @Service is auto-detected
// via component scanning.
// -----------------------------------------------------------------------------

// -----------------------------------------------------------------------------
// KEY CONVERSION PATTERNS:
// -----------------------------------------------------------------------------
// 1.  @Service replaces the application-scope singleton CFC — auto-detected by Spring
// 2.  cacheGet()/cachePut() → @Cacheable (declarative caching annotation)
// 3.  cacheRemove() → @CacheEvict (declarative cache eviction)
// 4.  writeLog()/<cflog> → SLF4J Logger: log.info() with {} placeholder syntax
// 5.  DAO/DataMgr CFC → Spring Data JPA repository (implementations auto-generated)
// 6.  No interface required (but recommended for testability)
// 7.  @Transactional manages database transactions (replaces <cftransaction>)
// 8.  @Transactional(readOnly = true) optimizes read-only queries
// 9.  Throw custom exceptions instead of returning null/empty struct (fail-fast pattern)
// 10. Java records replace CFML structs for DTOs (immutable, typed)
// 11. Dynamic <cfquery> + <cfqueryparam> filters → derived query methods / Specifications
// 12. Use structured logging with {} placeholders, not string concatenation
