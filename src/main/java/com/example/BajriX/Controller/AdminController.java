package com.example.BajriX.Controller;

import com.example.BajriX.Entity.Product;
import com.example.BajriX.Service.AdminService;
import com.example.BajriX.dto.AdminDashboardStatsResponse;
import com.example.BajriX.dto.ApiResponse;
import com.example.BajriX.dto.ProductRequestDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/BajriXadmin@")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * GET /api/v1/admin/stats
     * Returns total counts of products, listings, and sellers.
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminDashboardStatsResponse>> getDashboardStats() {
        AdminDashboardStatsResponse stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.ok("Admin stats retrieved successfully", stats));
    }

    /**
     * GET /api/v1/admin/products?page=0&size=10
     * Returns paginated list of products for the admin table.
     */
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<Page<Product>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = adminService.getAllProducts(pageable);
        return ResponseEntity.ok(ApiResponse.ok("Products retrieved successfully", products));
    }

    /**
     * GET /api/v1/admin/products/{id}
     * Fetch a specific product by ID.
     */
    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable Long id) {
        Product product = adminService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.ok("Product details retrieved", product));
    }

    /**
     * POST /api/v1/admin/products
     * Create a brand new product in the master catalogue.
     */
    @PostMapping("/products")
    public ResponseEntity<ApiResponse<Product>> createProduct(@Valid @RequestBody ProductRequestDTO req) {
        Product created = adminService.createProduct(req);
        return ResponseEntity.ok(ApiResponse.ok("Product created successfully", created));
    }

    /**
     * PUT /api/v1/admin/products/{id}
     * Update an existing product's details.
     */
    @PutMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO req
    ) {
        Product updated = adminService.updateProduct(id, req);
        return ResponseEntity.ok(ApiResponse.ok("Product updated successfully", updated));
    }

    /**
     * DELETE /api/v1/admin/products/{id}
     * Delete a product from catalogue.
     */
    @DeleteMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        adminService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.ok("Product deleted successfully", null));
    }
}
