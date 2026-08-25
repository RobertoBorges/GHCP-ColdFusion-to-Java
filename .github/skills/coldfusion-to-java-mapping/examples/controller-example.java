// =============================================================================
// ColdFusion (CFML) to Java Controller Conversion Example
// Part of the ColdFusion-to-Java Migration Framework
// =============================================================================

// -----------------------------------------------------------------------------
// BEFORE: ColdFusion controller page (product.cfm) backed by a service CFC
// A classic legacy CFML pattern: a .cfm page dispatches on a URL "action"
// and delegates to an application-scope singleton CFC (application.productService).
// -----------------------------------------------------------------------------
/*
<!--- product.cfm --->
<cfparam name="url.action" default="index">
<cfparam name="url.id" default="0">

<cfswitch expression="#url.action#">

    <!--- index: list all products --->
    <cfcase value="index">
        <cfset products = application.productService.getAllProducts()>
        <cfinclude template="views/products/index.cfm">
    </cfcase>

    <!--- show: single product --->
    <cfcase value="show">
        <cfset product = application.productService.getProductById(url.id)>
        <cfif not isStruct(product) or structIsEmpty(product)>
            <cfheader statuscode="404" statustext="Not Found">
            <cfinclude template="views/errors/404.cfm">
            <cfabort>
        </cfif>
        <cfinclude template="views/products/show.cfm">
    </cfcase>

    <!--- store: create from a submitted form --->
    <cfcase value="store">
        <cfset errors = []>
        <!--- Manual validation (typeless CFML) --->
        <cfif not len(trim(form.name)) or len(form.name) gt 255>
            <cfset arrayAppend(errors, "Name is required and must be <= 255 chars")>
        </cfif>
        <cfif not isNumeric(form.price) or form.price lt 0>
            <cfset arrayAppend(errors, "Price must be numeric and >= 0")>
        </cfif>

        <cfif arrayLen(errors)>
            <cfset request.errors = errors>
            <cfinclude template="views/products/create.cfm">
        <cfelse>
            <cfset product = application.productService.createProduct(form)>
            <cfset session.flash.success = "Product created successfully.">
            <cflocation url="product.cfm?action=show&id=#product.id#" addtoken="false">
        </cfif>
    </cfcase>

    <!--- update: modify an existing product --->
    <cfcase value="update">
        <cfset application.productService.updateProduct(url.id, form)>
        <cfset session.flash.success = "Product updated successfully.">
        <cflocation url="product.cfm?action=show&id=#url.id#" addtoken="false">
    </cfcase>

    <!--- destroy: delete a product --->
    <cfcase value="destroy">
        <cfset application.productService.deleteProduct(url.id)>
        <cfset session.flash.success = "Product deleted successfully.">
        <cflocation url="product.cfm?action=index" addtoken="false">
    </cfcase>

</cfswitch>
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

    // Constructor injection replaces createObject("component",...) / application-scope wiring
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // GET /products — replaces: <cfcase value="index">
    @GetMapping
    public String index(Model model) {
        var products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "products/index"; // Thymeleaf template: templates/products/index.html
    }

    // GET /products/{id} — replaces: <cfcase value="show">
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

    // POST /products — replaces: <cfcase value="store">
    @PostMapping
    public String store(
            @Valid @ModelAttribute("product") CreateProductRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        // BindingResult replaces the manual <cfif>/isNumeric()/len() validation block
        if (bindingResult.hasErrors()) {
            return "products/create";
        }

        var product = productService.createProduct(request);

        // RedirectAttributes replaces session.flash.success + <cflocation>
        redirectAttributes.addFlashAttribute("success", "Product created successfully.");
        return "redirect:/products/" + product.getId();
    }

    // POST /products/{id} — replaces: <cfcase value="update">
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

    // POST /products/{id}/delete — replaces: <cfcase value="destroy">
    @PostMapping("/{id}/delete")
    public String destroy(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deleteProduct(id);

        redirectAttributes.addFlashAttribute("success", "Product deleted successfully.");
        return "redirect:/products";
    }
}

// -----------------------------------------------------------------------------
// DTO for Create (dto/CreateProductRequest.java)
// Uses Jakarta Bean Validation annotations (replaces manual <cfif>/isValid() checks)
// -----------------------------------------------------------------------------

/*
package com.example.app.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateProductRequest(

    // len(trim(name)) and len <= 255 → @NotBlank + @Size
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    String name,

    // isNumeric(price) and price >= 0 → @NotNull + @DecimalMin
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must be at least 0")
    BigDecimal price,

    // optional field → no validation annotations needed (nullable by default)
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
// 1.  application-scope singleton CFC → Spring @Service bean + constructor injection
// 2.  Add SLF4J Logger (replaces <cflog> / writeLog()): LoggerFactory.getLogger(...)
// 3.  Manual <cfif>/isValid()/isNumeric() checks → @Valid on DTO + Jakarta Bean Validation
// 4.  <cfheader statuscode="404"> + <cfabort> → throw ResponseStatusException / service layer
// 5.  <cfinclude template="views/products/index.cfm"> → Model.addAttribute() + return template path
// 6.  <cflocation url="..."> → return "redirect:/path"
// 7.  session.flash.success → RedirectAttributes.addFlashAttribute("success", "msg")
// 8.  Hidden CSRF token / <cftoken> → automatic when using th:action + Spring Security
// 9.  url.id / <cfparam> + service lookup → @PathVariable Long id + service lookup
// 10. Typeless form/url scope values → typed Java records with annotation-based validation
// 11. One controller class per file (Java convention) — split the <cfswitch> into methods
// 12. Use @RequestMapping at class level for the common path prefix
