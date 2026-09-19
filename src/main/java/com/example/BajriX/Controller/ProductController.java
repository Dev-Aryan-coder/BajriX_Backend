package com.example.BajriX.Controller;

import com.example.BajriX.Entity.Product;
import com.example.BajriX.Service.ProductService;
import com.example.BajriX.dto.ApiResponse;
import com.example.BajriX.dto.ProductDetailResponse;
import com.example.BajriX.dto.ProductSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// buyer-facing endpoints for searching and viewing products
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // search & browse products
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductSummaryResponse>>> searchProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductSummaryResponse> result = productService.searchProducts(search, category, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Products fetched successfully", result));
    }

    // product detail view showing side-by-side comparison of active approved seller listings
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetail(@PathVariable Long id) {
        ProductDetailResponse detail = productService.getProductDetail(id);
        return ResponseEntity.ok(ApiResponse.ok("Product details loaded", detail));
    }

    // autocomplete helper when adding products to catalogue
    @GetMapping("/autocomplete")
    public ResponseEntity<ApiResponse<List<Product>>> autocompleteCatalogue(@RequestParam String query) {
        List<Product> matches = productService.searchCatalogue(query);
        return ResponseEntity.ok(ApiResponse.ok("Matches found", matches));
    }
}