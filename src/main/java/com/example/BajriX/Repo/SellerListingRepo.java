package com.example.BajriX.Repo;

import com.example.BajriX.Entity.SellerListing;
import com.example.BajriX.Entity.SellerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Why this repository exists:
 * This is where we enforce critical business filtering and data isolation rules:
 *
 * 1. Buyer Security Boundary:
 *    - Buyers should ONLY see listings where:
 *      a) The listing is active (`l.isActive = true`).
 *      b) The seller is APPROVED (`l.seller.status = APPROVED`).
 *    - Any seller who is PENDING or REJECTED, or any listing marked inactive, is filtered out at the DB level.
 *    - Results are ordered by price ascending (`ORDER BY l.price ASC`) so buyers immediately see the best deal.
 *
 * 2. Seller Isolation:
 *    - `findBySellerId`: When a seller opens their dashboard, they only query their own listings.
 *    - `existsBySellerIdAndProductId`: Ensures one seller cannot create multiple listings for the same product.
 *    - `findByIdAndSellerId`: When updating or stopping a listing, this verifies ownership.
 */
@Repository
public interface SellerListingRepo extends JpaRepository<SellerListing, Long> {

    // Query for the Buyer Product Detail Page:
    // Fetches all approved, active seller listings for a given product, sorted with the cheapest price on top.
    // Uses JOIN FETCH on seller so we don't suffer from N+1 select queries when showing seller shop names.
    @Query("SELECT l FROM SellerListing l JOIN FETCH l.seller WHERE l.product.id = :productId AND l.isActive = true AND l.seller.status = :status ORDER BY l.price ASC")
    List<SellerListing> findActiveApprovedListingsForProduct(@Param("productId") Long productId, @Param("status") SellerStatus status);

    // Query for the Seller Dashboard:
    // Returns a paginated list of all listings belonging to this specific seller (both active and paused).
    Page<SellerListing> findBySellerId(Long sellerId, Pageable pageable);

    // Duplicate check:
    // Used before creating a new listing to make sure this seller hasn't already listed this product.
    boolean existsBySellerIdAndProductId(Long sellerId, Long productId);

    // Security check:
    // Used before update/deactivate to guarantee that listing #X actually belongs to seller #Y.
    Optional<SellerListing> findByIdAndSellerId(Long id, Long sellerId);
}