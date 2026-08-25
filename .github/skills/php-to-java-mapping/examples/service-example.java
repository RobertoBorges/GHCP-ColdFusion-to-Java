// =============================================================================
// PHP to Java Service Conversion Example
// Part of the PHP-to-Java Migration Framework
// =============================================================================

// -----------------------------------------------------------------------------
// BEFORE: PHP Laravel Service (app/Services/ProductService.php)
// -----------------------------------------------------------------------------
/*
<?php

namespace App\Services;

use App\Models\Product;
use App\Repositories\ProductRepository;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Facades\Log;

class ProductService
{
    protected $productRepository;

    public function __construct(ProductRepository $productRepository)
    {
        $this->productRepository = $productRepository;
    }

    public function getAllProducts()
    {
        return Cache::remember('products.all', 3600, function () {
            Log::info('Fetching all products from database');
            return $this->productRepository->all();
        });
    }

    public function getProductById($id)
    {
        $cacheKey = "products.{$id}";

        return Cache::remember($cacheKey, 3600, function () use ($id) {
            return $this->productRepository->find($id);
        });
    }

    public function createProduct(array $data)
    {
        $product = $this->productRepository->create($data);

        Cache::forget('products.all');
        Log::info('Product created', ['id' => $product->id]);

        return $product;
    }

    public function updateProduct($id, array $data)
    {
        $product = $this->productRepository->update($id, $data);

        Cache::forget('products.all');
        Cache::forget("products.{$id}");
        Log::info('Product updated', ['id' => $id]);

        return $product;
    }

    public function deleteProduct($id)
    {
        $this->productRepository->delete($id);

        Cache::forget('products.all');
        Cache::forget("products.{$id}");
        Log::info('Product deleted', ['id' => $id]);
    }

    public function searchProducts($query, $filters = [])
    {
        return $this->productRepository
            ->where('name', 'like', "%{$query}%")
            ->when($filters['category'] ?? null, function ($q, $category) {
                return $q->where('category_id', $category);
            })
            ->when($filters['min_price'] ?? null, function ($q, $minPrice) {
                return $q->where('price', '>=', $minPrice);
            })
            ->when($filters['max_price'] ?? null, function ($q, $maxPrice) {
                return $q->where('price', '<=', $maxPrice);
            })
            ->get();
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

// @Service registers this class in the Spring ApplicationContext (replaces Service Provider binding)
@Service
@Transactional(readOnly = true) // Default read-only transactions for query methods
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    // Constructor injection (same pattern as Laravel; Spring auto-wires single-constructor beans)
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Cache::remember('products.all', 3600, ...) → @Cacheable
    @Cacheable(value = "products", key = "'all'")
    public List<Product> getAllProducts() {
        log.info("Fetching all products from database");
        return productRepository.findAll();
    }

    // Cache::remember("products.{$id}", 3600, ...) → @Cacheable with dynamic key
    @Cacheable(value = "products", key = "#id")
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    // Cache::forget('products.all') → @CacheEvict
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

    // Replaces Eloquent ->where()->when()->get() chain with repository query methods
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
// Java record replaces PHP associative arrays for typed, immutable data
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
// Replaces abort(404) and ModelNotFoundException
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
// Replaces config/cache.php
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
// Unlike Laravel's Service Providers, @Service is auto-detected via component scanning.
// -----------------------------------------------------------------------------

// -----------------------------------------------------------------------------
// KEY CONVERSION PATTERNS:
// -----------------------------------------------------------------------------
// 1.  @Service replaces Service Provider registration — auto-detected by Spring
// 2.  Cache::remember() → @Cacheable (declarative caching annotation)
// 3.  Cache::forget() → @CacheEvict (declarative cache eviction)
// 4.  Log::info() → SLF4J Logger: log.info() with {} placeholder syntax
// 5.  Repository pattern maps directly — Spring Data JPA generates implementations
// 6.  No interface required (but recommended for testability)
// 7.  @Transactional manages database transactions (replaces DB::transaction())
// 8.  @Transactional(readOnly = true) optimizes read-only queries
// 9.  Throw custom exceptions instead of returning null (fail-fast pattern)
// 10. Java records replace PHP associative arrays for DTOs (immutable, typed)
// 11. ->when() conditional queries → if-statements or Specifications pattern
// 12. Use structured logging with {} placeholders, not string concatenation
