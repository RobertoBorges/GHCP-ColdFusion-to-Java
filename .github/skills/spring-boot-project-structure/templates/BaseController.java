// =============================================================================
// Spring Boot Base Controller Template
// Part of the PHP-to-Java Migration Framework
// =============================================================================
// Base controller with common functionality for all controllers.
// Replaces Laravel's app/Http/Controllers/Controller.php
// =============================================================================

package com.example.app.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Base controller providing common utilities for MVC controllers.
 * Inherit from this class instead of using raw @Controller.
 *
 * Replaces Laravel's base Controller class.
 */
public abstract class BaseController {

    // ==========================================================================
    // Flash Messages (replaces Laravel's ->with() / session()->flash())
    //
    // Laravel: return redirect()->with('success', 'Product created.');
    // Spring: redirectAttributes.addFlashAttribute("success", "Product created.");
    //
    // In Thymeleaf templates, access flash attributes:
    //   <div th:if="${success}" th:text="${success}" class="alert alert-success"></div>
    //   <div th:if="${error}" th:text="${error}" class="alert alert-danger"></div>
    // ==========================================================================

    /**
     * Sets a success flash message for the next request.
     * Replaces: return redirect()->with('success', $message);
     */
    protected void flashSuccess(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("success", message);
    }

    /**
     * Sets an error flash message for the next request.
     * Replaces: return redirect()->with('error', $message);
     */
    protected void flashError(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("error", message);
    }

    /**
     * Sets a warning flash message for the next request.
     */
    protected void flashWarning(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("warning", message);
    }

    /**
     * Sets an info flash message for the next request.
     */
    protected void flashInfo(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("info", message);
    }

    // ==========================================================================
    // Current User Helpers (replaces Laravel's Auth facade)
    //
    // Laravel: Auth::id(), Auth::user(), Auth::check()
    // Spring: SecurityContextHolder or @AuthenticationPrincipal annotation
    //
    // Alternative: Use @AuthenticationPrincipal directly in controller methods:
    //   @GetMapping("/profile")
    //   public String profile(@AuthenticationPrincipal UserDetails user) { ... }
    // ==========================================================================

    /**
     * Gets the current Authentication object.
     * Replaces: Auth::user()
     */
    protected Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Gets the current authenticated username.
     * Replaces: Auth::user()->name or Auth::user()->email
     */
    protected String getCurrentUsername() {
        Authentication auth = getCurrentAuthentication();
        return auth != null ? auth.getName() : null;
    }

    /**
     * Checks if a user is authenticated.
     * Replaces: Auth::check() or @auth directive
     */
    protected boolean isAuthenticated() {
        Authentication auth = getCurrentAuthentication();
        return auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());
    }

    /**
     * Checks if the current user has a specific role.
     * Replaces: Auth::user()->hasRole('admin') or Gate::allows('admin')
     */
    protected boolean hasRole(String role) {
        Authentication auth = getCurrentAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    // ==========================================================================
    // Redirect Helpers
    // ==========================================================================

    /**
     * Builds a redirect URL.
     * Replaces: return redirect()->route('products.show', $id);
     *
     * Usage: return redirect("/products/" + id);
     */
    protected String redirect(String path) {
        return "redirect:" + path;
    }

    /**
     * Redirects to the referer or a fallback URL.
     * Replaces: return back();
     *
     * Note: In Spring MVC, the Referer header is available but not always reliable.
     * Consider using a hidden input or session attribute for return URLs.
     */
    protected String redirectBack(String fallbackUrl) {
        return "redirect:" + fallbackUrl;
    }

    // ==========================================================================
    // Model Helpers
    // ==========================================================================

    /**
     * Adds common attributes to the model for all views.
     * Call this in each controller method, or use @ModelAttribute.
     *
     * Replaces: View::share() or View::composer() in AppServiceProvider
     */
    protected void addCommonAttributes(Model model) {
        model.addAttribute("currentUser", getCurrentUsername());
        model.addAttribute("isAuthenticated", isAuthenticated());
    }
}

// =============================================================================
// Usage Example — ProductController extends BaseController
// =============================================================================

/*
@Controller
@RequestMapping("/products")
public class ProductController extends BaseController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String index(Model model) {
        addCommonAttributes(model);
        model.addAttribute("products", productService.getAllProducts());
        return "products/index";
    }

    @PostMapping
    public String store(@Valid @ModelAttribute CreateProductRequest request,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "products/create";
        }

        var product = productService.createProduct(request);
        flashSuccess(redirectAttributes, "Product created successfully.");
        return redirect("/products/" + product.getId());
    }

    @PostMapping("/{id}/delete")
    public String destroy(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deleteProduct(id);
        flashSuccess(redirectAttributes, "Product deleted.");
        return redirect("/products");
    }
}
*/

// =============================================================================
// REST API Base Controller (for API-only controllers)
// Replaces Laravel API controller conventions.
// =============================================================================

/*
package com.example.app.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

public abstract class ApiBaseController {

    // Standard JSON success response
    // Replaces: return response()->json(['success' => true, 'data' => $data]);
    protected <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity.ok(new ApiResponse<>(true, null, data));
    }

    protected <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, data));
    }

    // Standard JSON created response (201)
    protected <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Created", data));
    }

    // Standard JSON error response
    // Replaces: return response()->json(['success' => false, 'message' => $msg], 400);
    protected ResponseEntity<ApiResponse<Void>> error(String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(false, message, null));
    }

    // Standard not found response
    // Replaces: abort(404)
    protected ResponseEntity<ApiResponse<Void>> notFound(String message) {
        return error(message, HttpStatus.NOT_FOUND);
    }
}

// Standardized API response wrapper
record ApiResponse<T>(boolean success, String message, T data) {}
*/

// =============================================================================
// Global Exception Handler
// Replaces: app/Exceptions/Handler.php
//
// Place in: exception/GlobalExceptionHandler.java
// =============================================================================

/*
package com.example.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Replaces: Handler::render() for ModelNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", ex.getMessage()));
    }

    // Replaces: Handler::render() for ValidationException
    // Converts @Valid errors to a structured response
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                    e -> e.getField(),
                    e -> e.getDefaultMessage(),
                    (a, b) -> a));

        return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Validation failed", "errors", errors));
    }

    // Catch-all for unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "An unexpected error occurred"));
    }
}
*/
