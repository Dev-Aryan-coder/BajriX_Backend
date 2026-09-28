package com.example.BajriX.Service;

import com.example.BajriX.Entity.Product;
import com.example.BajriX.Repo.ProductRepo;
import com.example.BajriX.Repo.SellerListingRepo;
import com.example.BajriX.Repo.SellerRepo;
import com.example.BajriX.dto.AdminDashboardStatsResponse;
import com.example.BajriX.dto.ProductRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final ProductRepo productRepo;
    private final SellerListingRepo listingRepo;
    private final SellerRepo sellerRepo;

    public AdminService(ProductRepo productRepo, SellerListingRepo listingRepo, SellerRepo sellerRepo) {
        this.productRepo = productRepo;
        this.listingRepo = listingRepo;
        this.sellerRepo = sellerRepo;
    }

    /**
     * 1. Dashboard Overview Counts
     */
    public AdminDashboardStatsResponse getDashboardStats() {
        long productCount = productRepo.count();
        long listingCount = listingRepo.count();
        long sellerCount = sellerRepo.count();

        return new AdminDashboardStatsResponse(productCount, listingCount, sellerCount);
    }

    /**
     * 2. CREATE: Add new catalogue product
     */
    @Transactional
    public Product createProduct(ProductRequestDTO req) {
        Product product = new Product(
                req.getName().trim(),
                req.getCategory().trim(),
                req.getDescription() != null ? req.getDescription().trim() : null
        );
        return productRepo.save(product);
    }

    /**
     * 3. READ: Get all products with pagination
     */
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepo.findAll(pageable);
    }

    /**
     * 4. READ: Get single product by ID
     */
    public Product getProductById(Long id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
    }

    /**
     * 5. UPDATE: Update product information
     */
    @Transactional
    public Product updateProduct(Long id, ProductRequestDTO req) {
        Product product = getProductById(id);
        product.setName(req.getName().trim());
        product.setCategory(req.getCategory().trim());
        if (req.getDescription() != null) {
            product.setDescription(req.getDescription().trim());
        }
        return productRepo.save(product);
    }

    /**
     * 6. DELETE: Remove product from catalogue
     */
    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        
        // Safety rule: Cannot delete product if sellers currently have active listings for it
        if (listingRepo.existsById(id)) { // or check active count
            throw new IllegalStateException("Cannot delete product: Existing seller listings are attached to it. Please remove or archive those listings first.");
        }

        productRepo.delete(product);
    }
}
