package com.example.BajriX.Controller;

import com.example.BajriX.Entity.Seller;
import com.example.BajriX.Service.AuthService;
import com.example.BajriX.Service.SellerListingService;
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
 * Why this controller exists:
 * Exposes seller-scoped listing endpoints:
 * - GET: view own listings (dashboard)
 * - POST: create listing
 * - PUT: update listing (price, stock, MOQ, version check)
 * - PATCH: toggle active (stop selling)
 *
 * Real Authorization Enforcement:
 * Every endpoint here requires the `X-Session-Token` HTTP header.
 * The server resolves the actual caller identity via `authService.resolveSellerByToken(sessionToken)`.
 * It strictly verifies that the authenticated seller matches the `sellerId` in the path.
 * An attacker CANNOT tamper with another seller's listings by just changing the seller ID in the URL.
 */
@RestController
@RequestMapping("/api/v1/sellers/{sellerId}/listings")
public class SellerListingController {

    private final SellerListingService listingService;
    private final AuthService authService;

    public SellerListingController(SellerListingService listingService, AuthService authService) {
        this.listingService = listingService;
        this.authService = authService;
    }

    /**
     * Helper to verify authorization boundary:
     * 1. Resolves caller from session token.
     * 2. Asserts caller ID == URL sellerId.
     */
    private Seller authenticateAndVerifyOwner(String sessionToken, Long pathSellerId) {
        Seller caller = authService.resolveSellerByToken(sessionToken);
        if (!caller.getId().equals(pathSellerId)) {
            throw new SecurityException("Access denied: You are logged in as '" + caller.getName() + 
                    "' (ID: " + caller.getId() + ") and cannot access or modify listings belonging to seller ID: " + pathSellerId);
        }
        return caller;
    }

    // Get seller dashboard listings - strictly scoped to authenticated seller
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ListingResponse>>> getSellerListings(
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken,
            @PathVariable Long sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Seller caller = authenticateAndVerifyOwner(sessionToken, sellerId);
        Pageable pageable = PageRequest.of(page, size);
        Page<ListingResponse> listings = listingService.getSellerListings(caller.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok("Seller listings retrieved", listings));
    }

    // Add product / create listing
    @PostMapping
    public ResponseEntity<ApiResponse<ListingResponse>> createListing(
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken,
            @PathVariable Long sellerId,
            @Valid @RequestBody CreateListingRequest req
    ) {
        Seller caller = authenticateAndVerifyOwner(sessionToken, sellerId);
        ListingResponse created = listingService.createListing(caller.getId(), req);
        return ResponseEntity.ok(ApiResponse.ok("Listing created successfully", created));
    }

    // Update price, stock, or MOQ with optimistic locking check
    @PutMapping("/{listingId}")
    public ResponseEntity<ApiResponse<ListingResponse>> updateListing(
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken,
            @PathVariable Long sellerId,
            @PathVariable Long listingId,
            @Valid @RequestBody UpdateListingRequest req
    ) {
        Seller caller = authenticateAndVerifyOwner(sessionToken, sellerId);
        ListingResponse updated = listingService.updateListing(caller.getId(), listingId, req);
        return ResponseEntity.ok(ApiResponse.ok("Listing updated successfully", updated));
    }

    // Toggle active / stop selling
    @PatchMapping("/{listingId}/status")
    public ResponseEntity<ApiResponse<ListingResponse>> toggleActive(
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken,
            @PathVariable Long sellerId,
            @PathVariable Long listingId,
            @RequestParam boolean active
    ) {
        Seller caller = authenticateAndVerifyOwner(sessionToken, sellerId);
        ListingResponse updated = listingService.toggleListingActive(caller.getId(), listingId, active);
        String msg = active ? "Listing reactivated" : "Listing deactivated (stopped selling)";
        return ResponseEntity.ok(ApiResponse.ok(msg, updated));
    }
}