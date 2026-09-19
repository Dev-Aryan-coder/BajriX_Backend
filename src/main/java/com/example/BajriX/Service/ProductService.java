package com.example.BajriX.Service;

import com.example.BajriX.Entity.Product;
import com.example.BajriX.Entity.SellerListing;
import com.example.BajriX.Entity.SellerStatus;
import com.example.BajriX.Repo.ProductRepo;
import com.example.BajriX.Repo.SellerListingRepo;
import com.example.BajriX.dto.ListingResponse;
import com.example.BajriX.dto.ProductDetailResponse;
import com.example.BajriX.dto.ProductSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Why this service exists:
 * Powers the buyer discovery and side-by-side comparison experience.
 *
 * How it works:
 * 1. `searchProducts`:
 *    - For the buyer's home/browse page.
 *    - Fetches catalogue products based on search keyword and category.
 *    - For each product, it calculates:
 *      a) Lowest available price from approved sellers (e.g. "Starting from ₹385").
 *      b) Total number of approved sellers offering it (e.g. "3 sellers").
 *    - Paginated so large numbers of products load fast.
 *
 * 2. `getProductDetail`:
 *    - The core comparison screen (the UltraTech Cement example from the brief).
 *    - Fetches the product information and all active listings from APPROVED sellers.
 *    - Sorted with the lowest price first, so buyers immediately see who has the best deal.
 *    - Excludes listings from pending or rejected sellers.
 */
@Service
public class ProductService {

    private final ProductRepo productRepo;
    private final SellerListingRepo listingRepo;

    public ProductService(ProductRepo productRepo, SellerListingRepo listingRepo) {
        this.productRepo = productRepo;
        this.listingRepo = listingRepo;
    }

    /**
     * Buyer browse & search:
     * Returns summary cards showing product name, category, starting price, and seller count.
     */
    public Page<ProductSummaryResponse> searchProducts(String search, String category, Pageable pageable) {
        Page<Product> products = productRepo.searchProducts(search, category, pageable);

        return products.map(product -> {
            // Find active listings from approved sellers only
            List<SellerListing> listings = listingRepo.findActiveApprovedListingsForProduct(product.getId(), SellerStatus.APPROVED);
            
            // Since listings are ordered by price ASC, the first element is the lowest price
            BigDecimal lowest = listings.isEmpty() ? null : listings.get(0).getPrice();
            int count = listings.size();

            return new ProductSummaryResponse(
                    product.getId(),
                    product.getName(),
                    product.getCategory(),
                    product.getDescription(),
                    lowest,
                    count
            );
        });
    }

    /**
     * Buyer comparison screen:
     * Shows all approved sellers side-by-side for a single product with price, stock, and MOQ.
     */
    public ProductDetailResponse getProductDetail(Long productId) {
        // Step 1: Find product info
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));

        // Step 2: Fetch all active listings from APPROVED sellers only
        List<SellerListing> listings = listingRepo.findActiveApprovedListingsForProduct(productId, SellerStatus.APPROVED);

        // Step 3: Map to response DTOs
        List<ListingResponse> listingResponses = listings.stream().map(l -> {
            ListingResponse res = new ListingResponse();
            res.setId(l.getId());
            res.setSellerId(l.getSeller().getId());
            res.setSellerName(l.getSeller().getName());
            res.setSellerStatus(l.getSeller().getStatus().name());
            res.setProductId(product.getId());
            res.setProductName(product.getName());
            res.setProductCategory(product.getCategory());
            res.setPrice(l.getPrice());
            res.setStockQuantity(l.getStockQuantity());
            res.setMinOrderQuantity(l.getMinOrderQuantity());
            res.setIsActive(l.getIsActive());
            res.setVersion(l.getVersion());
            res.setUpdatedAt(l.getUpdatedAt());
            return res;
        }).collect(Collectors.toList());

        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getDescription(),
                listingResponses
        );
    }

    /**
     * Autocomplete search for sellers when adding a product to catalogue.
     */
    public List<Product> searchCatalogue(String query) {
        return productRepo.findTop10ByNameContainingIgnoreCase(query);
    }
}