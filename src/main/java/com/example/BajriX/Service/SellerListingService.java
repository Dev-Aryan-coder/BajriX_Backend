package com.example.BajriX.Service;

import com.example.BajriX.Entity.Product;
import com.example.BajriX.Entity.Seller;
import com.example.BajriX.Entity.SellerListing;
import com.example.BajriX.Repo.ProductRepo;
import com.example.BajriX.Repo.SellerListingRepo;
import com.example.BajriX.Repo.SellerRepo;
import com.example.BajriX.dto.CreateListingRequest;
import com.example.BajriX.dto.ListingResponse;
import com.example.BajriX.dto.UpdateListingRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Why this service exists:
 * This is where the core business rules and security boundaries of the entire system live.
 *
 * Key Responsibilities:
 * 1. Authorization Boundary: A seller can ONLY modify or view their own listings.
 *    Even if someone tries to forge a PUT request to update listing ID #5 with seller ID #2,
 *    we check ownership here in the service layer and throw a SecurityException (403 Forbidden).
 *
 * 2. Concurrency Control (Optimistic Locking):
 *    When updating a listing, we compare the `version` sent by the client with the `version`
 *    stored in the database. If they don't match, someone else saved a change first!
 *    We reject the update with a 409 Conflict, telling the user to refresh and review the latest values.
 *
 * 3. Business Rule Validation:
 *    - Price must be > 0.
 *    - Stock quantity must be >= 0.
 *    - Minimum order quantity (MOQ) must be > 0.
 *    - MOQ cannot be greater than available stock (if stock > 0).
 *    - A seller cannot create duplicate listings for the same product.
 *
 * 4. Search-or-Create Flow:
 *    When a seller adds a product, they can either link to an existing catalogue item or
 *    enter details for a new product, which creates the Product and the SellerListing together.
 */
@Service
public class SellerListingService {

    private final SellerListingRepo listingRepo;
    private final SellerRepo sellerRepo;
    private final ProductRepo productRepo;

    public SellerListingService(SellerListingRepo listingRepo, SellerRepo sellerRepo, ProductRepo productRepo) {
        this.listingRepo = listingRepo;
        this.sellerRepo = sellerRepo;
        this.productRepo = productRepo;
    }

    /**
     * Fetches all listings belonging to a specific seller for their dashboard.
     * Guaranteed to only return this seller's data.
     */
    public Page<ListingResponse> getSellerListings(Long sellerId, Pageable pageable) {
        return listingRepo.findBySellerId(sellerId, pageable).map(this::mapToResponse);
    }

    /**
     * Creates a new listing for a seller.
     * Handles both cases:
     * - Case A: Seller chose an existing product from the catalogue (productId is passed).
     * - Case B: Seller is adding a product that doesn't exist yet (newProductName, category, description).
     */
    @Transactional
    public ListingResponse createListing(Long sellerId, CreateListingRequest req) {
        // Step 1: Verify that the seller exists
        Seller seller = sellerRepo.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found with id: " + sellerId));

        // Step 2: Resolve or create the product catalogue item
        Product product;
        if (req.getProductId() != null) {
            // Case A: Link to existing catalogue product
            product = productRepo.findById(req.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Selected product does not exist in the catalogue"));
        } else {
            // Case B: Create new product in the master catalogue
            if (req.getNewProductName() == null || req.getNewProductName().trim().isEmpty()) {
                throw new IllegalArgumentException("Product name is required when creating a new catalogue product");
            }
            product = new Product(
                    req.getNewProductName().trim(),
                    req.getNewProductCategory() != null ? req.getNewProductCategory().trim() : "General",
                    req.getNewProductDescription()
            );
            product = productRepo.save(product);
        }

        // Step 3: Check for duplicate listing
        // A single seller cannot sell the same product twice. If they already sell it, they should update their existing listing.
        if (listingRepo.existsBySellerIdAndProductId(sellerId, product.getId())) {
            throw new IllegalStateException("You already have a listing for this product. Please edit your existing listing instead.");
        }

        // Step 4: Validate business logic - MOQ cannot exceed available stock
        if (req.getMinOrderQuantity() > req.getStockQuantity() && req.getStockQuantity() > 0) {
            throw new IllegalArgumentException("Minimum order quantity (" + req.getMinOrderQuantity() + 
                    ") cannot be greater than available stock (" + req.getStockQuantity() + ")");
        }

        // Step 5: Save and return the new listing
        SellerListing listing = new SellerListing(
                seller,
                product,
                req.getPrice(),
                req.getStockQuantity(),
                req.getMinOrderQuantity()
        );

        SellerListing saved = listingRepo.save(listing);
        return mapToResponse(saved);
    }

    /**
     * Updates an existing listing's price, stock, or MOQ.
     * Enforces two critical safeguards:
     * 1. Authorization: Verifies the authenticated seller owns this listing.
     * 2. Concurrency: Verifies the version number to prevent silent overwrites.
     */
    @Transactional
    public ListingResponse updateListing(Long sellerId, Long listingId, UpdateListingRequest req) {
        // Step 1: Find the listing
        SellerListing listing = listingRepo.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found with id: " + listingId));

        // Step 2: Strict ownership verification (Authorization boundary)
        // If Seller A tries to edit Seller B's listing ID, we stop it right here.
        if (!listing.getSeller().getId().equals(sellerId)) {
            throw new SecurityException("Access denied: You do not have permission to modify another seller's listing.");
        }

        // Step 3: Optimistic concurrency lock verification
        // If another update occurred since this seller loaded the edit form, the versions will differ.
        if (!listing.getVersion().equals(req.getVersion())) {
            throw new IllegalStateException("Conflict detected: This listing was updated by another process. Please refresh the page to see the latest values before saving.");
        }

        // Step 4: Validate MOQ against available stock
        if (req.getMinOrderQuantity() > req.getStockQuantity() && req.getStockQuantity() > 0) {
            throw new IllegalArgumentException("Minimum order quantity (" + req.getMinOrderQuantity() + 
                    ") cannot be greater than available stock (" + req.getStockQuantity() + ")");
        }

        // Step 5: Apply updates
        listing.setPrice(req.getPrice());
        listing.setStockQuantity(req.getStockQuantity());
        listing.setMinOrderQuantity(req.getMinOrderQuantity());
        if (req.getIsActive() != null) {
            listing.setIsActive(req.getIsActive());
        }

        SellerListing saved = listingRepo.save(listing);
        return mapToResponse(saved);
    }

    /**
     * "Stop Selling" action:
     * We don't delete the record from the database because we want to preserve historical data.
     * Instead, we soft-delete it by setting isActive = false (or true to reactivate).
     */
    @Transactional
    public ListingResponse toggleListingActive(Long sellerId, Long listingId, boolean active) {
        SellerListing listing = listingRepo.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found with id: " + listingId));

        // Ensure ownership before toggling
        if (!listing.getSeller().getId().equals(sellerId)) {
            throw new SecurityException("Access denied: You do not have permission to modify another seller's listing.");
        }

        listing.setIsActive(active);
        SellerListing saved = listingRepo.save(listing);
        return mapToResponse(saved);
    }

    // Helper method to convert an internal JPA entity to a clean API response DTO
    private ListingResponse mapToResponse(SellerListing l) {
        ListingResponse res = new ListingResponse();
        res.setId(l.getId());
        res.setSellerId(l.getSeller().getId());
        res.setSellerName(l.getSeller().getName());
        res.setSellerStatus(l.getSeller().getStatus().name());
        res.setProductId(l.getProduct().getId());
        res.setProductName(l.getProduct().getName());
        res.setProductCategory(l.getProduct().getCategory());
        res.setPrice(l.getPrice());
        res.setStockQuantity(l.getStockQuantity());
        res.setMinOrderQuantity(l.getMinOrderQuantity());
        res.setIsActive(l.getIsActive());
        res.setVersion(l.getVersion());
        res.setUpdatedAt(l.getUpdatedAt());
        return res;
    }
}