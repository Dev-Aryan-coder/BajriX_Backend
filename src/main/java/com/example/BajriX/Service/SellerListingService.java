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
 * Real Authorization Boundary:
 * The `sellerId` passed into every method here is NOT a self-reported number from the URL;
 * it has been resolved from the caller''s cryptographic session wristband (`X-Session-Token`).
 *
 * The ownership check:
 * `if (!listing.getSeller().getId().equals(authenticatedSellerId))`
 * is now a genuine security barrier. Even if an attacker knows Listing #12 belongs to Seller #3,
 * they cannot edit it unless they possess Seller #3''s secret wristband token.
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
     */
    public Page<ListingResponse> getSellerListings(Long authenticatedSellerId, Pageable pageable) {
        return listingRepo.findBySellerId(authenticatedSellerId, pageable).map(this::mapToResponse);
    }

    /**
     * Creates a new listing for a seller.
     * Supports search-or-create: links to existing product, or creates a new catalogue product.
     */
    @Transactional
    public ListingResponse createListing(Long authenticatedSellerId, CreateListingRequest req) {
        Seller seller = sellerRepo.findById(authenticatedSellerId)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found with id: " + authenticatedSellerId));

        Product product;
        if (req.getProductId() != null) {
            product = productRepo.findById(req.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Selected product does not exist in the catalogue"));
        } else {
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

        // Duplicate check: one seller cannot list the same product twice
        if (listingRepo.existsBySellerIdAndProductId(authenticatedSellerId, product.getId())) {
            throw new IllegalStateException("You already have a listing for this product. Please edit your existing listing instead.");
        }

        // Business rule: MOQ cannot exceed available stock
        if (req.getMinOrderQuantity() > req.getStockQuantity() && req.getStockQuantity() > 0) {
            throw new IllegalArgumentException("Minimum order quantity (" + req.getMinOrderQuantity() + 
                    ") cannot be greater than available stock (" + req.getStockQuantity() + ")");
        }

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
     * Updates an existing listing.
     * Enforces:
     * 1. Real ownership check (listing owner must equal verified wristband seller).
     * 2. Optimistic locking check (version must match).
     * 3. MOQ vs stock check.
     */
    @Transactional
    public ListingResponse updateListing(Long authenticatedSellerId, Long listingId, UpdateListingRequest req) {
        SellerListing listing = listingRepo.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found with id: " + listingId));

        // REAL OWNERSHIP CHECK:
        // Confirms that the listing owner matches the seller identified by the wristband token
        if (!listing.getSeller().getId().equals(authenticatedSellerId)) {
            throw new SecurityException("Access denied: You do not own this listing.");
        }

        // Concurrency lock check
        if (!listing.getVersion().equals(req.getVersion())) {
            throw new IllegalStateException("Conflict detected: This listing was updated by another process. Please refresh the page to see the latest values before saving.");
        }

        if (req.getMinOrderQuantity() > req.getStockQuantity() && req.getStockQuantity() > 0) {
            throw new IllegalArgumentException("Minimum order quantity (" + req.getMinOrderQuantity() + 
                    ") cannot be greater than available stock (" + req.getStockQuantity() + ")");
        }

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
     * "Stop Selling" soft delete.
     */
    @Transactional
    public ListingResponse toggleListingActive(Long authenticatedSellerId, Long listingId, boolean active) {
        SellerListing listing = listingRepo.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found with id: " + listingId));

        if (!listing.getSeller().getId().equals(authenticatedSellerId)) {
            throw new SecurityException("Access denied: You do not own this listing.");
        }

        listing.setIsActive(active);
        SellerListing saved = listingRepo.save(listing);
        return mapToResponse(saved);
    }

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