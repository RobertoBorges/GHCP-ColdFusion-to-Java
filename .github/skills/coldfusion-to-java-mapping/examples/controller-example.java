// =============================================================================
// PHP to Java Controller Conversion Example
// Part of the PHP-to-Java Migration Framework
// =============================================================================

// -----------------------------------------------------------------------------
// BEFORE: PHP Laravel Controller (app/Http/Controllers/ProductController.php)
// -----------------------------------------------------------------------------
/*
<?php

namespace App\Http\Controllers;

use App\Models\Product;
use App\Services\ProductService;
use Illuminate\Http\Request;

class ProductController extends Controller
{
    protected $productService;

    public function __construct(ProductService $productService)
    {
        $this->productService = $productService;
    }

    public function index()
    {
        $products = $this->productService->getAllProducts();
        return view('products.index', compact('products'));
    }

    public function show($id)
    {
        $product = $this->productService->getProductById($id);

        if (!$product) {
            abort(404);
        }

        return view('products.show', compact('product'));
    }

    public function store(Request $request)
    {
        $validated = $request->validate([
            'name' => 'required|max:255',
            'price' => 'required|numeric|min:0',
            'description' => 'nullable|string',
        ]);

        $product = $this->productService->createProduct($validated);

        return redirect()->route('products.show', $product->id)
            ->with('success', 'Product created successfully.');
    }

    public function update(Request $request, $id)
    {
        $validated = $request->validate([
            'name' => 'required|max:255',
            'price' => 'required|numeric|min:0',
            'description' => 'nullable|string',
        ]);

        $product = $this->productService->updateProduct($id, $validated);

        return redirect()->route('products.show', $id)
            ->with('success', 'Product updated successfully.');
    }

    public function destroy($id)
    {
        $this->productService->deleteProduct($id);

        return redirect()->route('products.index')
            ->with('success', 'Product deleted successfully.');
    }
}
*/

// -----------------------------------------------------------------------------
// AFTER: Spring Boot Controller (controller/ProductController.java)
// -----------------------------------------------------------------------------

package com.example.app.controller;

import com.example.app.dto.CreateProductRequest;
import com.example.app.dto.UpdateProductRequest;
import com.example.app.entity.Product;
import com.example.app.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    // Constructor injection (same pattern as Laravel)
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // GET /products — replaces: public function index()
    @GetMapping
    public String index(Model model) {
        var products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "products/index"; // Thymeleaf template: templates/products/index.html
    }

    // GET /products/{id} — replaces: public function show($id)
    @GetMapping("/{id}")
    public String show(@PathVariable Long id, Model model) {
        var product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "products/show";
    }

    // GET /products/create — show the create form
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("product", new CreateProductRequest());
        return "products/create";
    }

    // POST /products — replaces: public function store(Request $request)
    @PostMapping
    public String store(
            @Valid @ModelAttribute("product") CreateProductRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        // BindingResult replaces $request->validate() — validation errors go here
        if (bindingResult.hasErrors()) {
            return "products/create";
        }

        var product = productService.createProduct(request);

        // RedirectAttributes replaces ->with('success', 'message')
        redirectAttributes.addFlashAttribute("success", "Product created successfully.");
        return "redirect:/products/" + product.getId();
    }

    // POST /products/{id} — replaces: public function update(Request $request, $id)
    // Note: HTML forms only support GET/POST; use hidden _method for PUT in Thymeleaf
    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("product") UpdateProductRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "products/edit";
        }

        productService.updateProduct(id, request);

        redirectAttributes.addFlashAttribute("success", "Product updated successfully.");
        return "redirect:/products/" + id;
    }

    // POST /products/{id}/delete — replaces: public function destroy($id)
    @PostMapping("/{id}/delete")
    public String destroy(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deleteProduct(id);

        redirectAttributes.addFlashAttribute("success", "Product deleted successfully.");
        return "redirect:/products";
    }
}

// -----------------------------------------------------------------------------
// DTO for Create (dto/CreateProductRequest.java)
// Uses Jakarta Bean Validation annotations (replaces Laravel validation rules)
// -----------------------------------------------------------------------------

/*
package com.example.app.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateProductRequest(

    // 'required|max:255' → @NotBlank + @Size
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    String name,

    // 'required|numeric|min:0' → @NotNull + @DecimalMin
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must be at least 0")
    BigDecimal price,

    // 'nullable|string' → no validation annotations needed (nullable by default)
    String description

) {}
*/

// -----------------------------------------------------------------------------
// DTO for Update (dto/UpdateProductRequest.java)
// -----------------------------------------------------------------------------

/*
package com.example.app.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record UpdateProductRequest(

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    String name,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must be at least 0")
    BigDecimal price,

    String description

) {}
*/

// -----------------------------------------------------------------------------
// KEY CONVERSION PATTERNS:
// -----------------------------------------------------------------------------
// 1.  Constructor injection is identical in concept; Spring uses @Controller + constructor
// 2.  Add SLF4J Logger (replaces Log facade): LoggerFactory.getLogger(...)
// 3.  $request->validate([...]) → @Valid on DTO + Jakarta Bean Validation annotations
// 4.  abort(404) → throw ResponseStatusException or handled by service layer
// 5.  view('products.index', compact('products')) → Model.addAttribute() + return template path
// 6.  redirect()->route('name') → return "redirect:/path"
// 7.  ->with('success', 'msg') → RedirectAttributes.addFlashAttribute("success", "msg")
// 8.  @csrf in Blade → automatic when using th:action in Thymeleaf + Spring Security
// 9.  Route model binding ($product) → @PathVariable Long id + service lookup
// 10. PHP arrays for validation → Java records with annotation-based validation
// 11. One controller class per file (Java convention)
// 12. Use @RequestMapping at class level for common path prefix
