package com.example.BajriX.Controller;

import com.example.BajriX.Entity.Seller;
import com.example.BajriX.Service.SellerListingService;
import com.example.BajriX.config.CurrentSellerContext;
import com.example.BajriX.dto.ApiResponse;
import com.example.BajriX.dto.CreateListingRequest;
import com.example.BajriX.dto.ListingResponse;
import com.example.BajriX.dto.UpdateListingRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Seller Listing Endpoints:
 *
 * How the "Wristband" Authorization Works Here:
 * 1. `SessionAuthFilter` has already verified the `X-Session-Token` header before reaching this code.
 * 2. We retrieve the REAL authenticated seller via `CurrentSellerContext.get()`.
 * 3. We verify that the caller is not trying to act on another seller''s account:
 *    If the URL says `/sellers/7/listings/...` but the wristband belongs to Seller 3,
 *    we reject it immediately!
 * 4. When creating or updating listings, we use the VERIFIED seller ID from the wristband,
 *    so the ownership check is 100% genuine and impossible to forge.
 */
@RestController
@RequestMapping("/api/v1/sellers/{sellerId}/listings")
public class SellerListingController {

    private final SellerListingService listingService;

    public SellerListingController(SellerListingService listingService) {
        this.listingService = listingService;
    }

    /**
     * Helper to verify that the wristband matches the path variable.
     * Prevents a logged-in seller from tampering with someone else''s URL.
     */
    private Seller getVerifiedSeller(Long pathSellerId) {
        Seller caller = CurrentSellerContext.get();
        if (caller == null) {
            throw new SecurityException("Authentication required: Missing or invalid session wristband.");
        }
        if (!caller.getId().equals(pathSellerId)) {
            throw new SecurityException("Access denied: You are authenticated as '" + caller.getName() + 
                    "' (Seller ID: " + caller.getId() + ") and cannot access or modify listings for Seller ID: " + pathSellerId);
        }
        return caller;
    }

    // Get seller dashboard listings - strictly scoped to verified caller
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ListingResponse>>> getSellerListings(
            @PathVariable Long sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Seller verified = getVerifiedSeller(sellerId);
        Pageable pageable = PageRequest.of(page, size);
        Page<ListingResponse> listings = listingService.getSellerListings(verified.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok("Seller listings retrieved", listings));
    }

    // Add product / create listing
    @PostMapping
    public ResponseEntity<ApiResponse<ListingResponse>> createListing(
            @PathVariable Long sellerId,
            @Valid @RequestBody CreateListingRequest req
    ) {
        Seller verified = getVerifiedSeller(sellerId);
        ListingResponse created = listingService.createListing(verified.getId(), req);
        return ResponseEntity.ok(ApiResponse.ok("Listing created successfully", created));
    }

    // Update price, stock, or MOQ with optimistic locking check
    @PutMapping("/{listingId}")
    public ResponseEntity<ApiResponse<ListingResponse>> updateListing(
            @PathVariable Long sellerId,
            @PathVariable Long listingId,
            @Valid @RequestBody UpdateListingRequest req
    ) {
        Seller verified = getVerifiedSeller(sellerId);
        ListingResponse updated = listingService.updateListing(verified.getId(), listingId, req);
        return ResponseEntity.ok(ApiResponse.ok("Listing updated successfully", updated));
    }

    // Toggle active / stop selling
    @PatchMapping("/{listingId}/status")
    public ResponseEntity<ApiResponse<ListingResponse>> toggleActive(
            @PathVariable Long sellerId,
            @PathVariable Long listingId,
            @RequestParam boolean active
    ) {
        Seller verified = getVerifiedSeller(sellerId);
        ListingResponse updated = listingService.toggleListingActive(verified.getId(), listingId, active);
        String msg = active ? "Listing reactivated" : "Listing deactivated (stopped selling)";
        return ResponseEntity.ok(ApiResponse.ok(msg, updated));
    }
}