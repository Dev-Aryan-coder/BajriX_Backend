package com.example.BajriX.Entity;

/**
 * Why this enum exists:
 * In our marketplace, we can't just let anyone register and immediately show their
 * listings to buyers. Construction materials are critical purchases (if bad cement
 * or defective steel is delivered, it can ruin a building).
 *
 * So we have an approval lifecycle for sellers:
 *
 * 1. PENDING:
 *    - When a seller registers, their account starts here.
 *    - They are allowed to log into their dashboard and prepare their product listings,
 *      prices, and stock in advance.
 *    - HOWEVER, our buyer-facing APIs will strictly filter these out. Buyers will NOT
 *      see any listings from a PENDING seller.
 *
 * 2. APPROVED:
 *    - Once verified (e.g. by admin or during demo), the seller is marked APPROVED.
 *    - All their active listings immediately become visible to buyers on the marketplace.
 *
 * 3. REJECTED:
 *    - If a seller fails verification or violates platform rules.
 *    - Their listings remain hidden from buyers, and their dashboard indicates the rejection.
 */
public enum SellerStatus {
    PENDING,
    APPROVED,
    REJECTED
}