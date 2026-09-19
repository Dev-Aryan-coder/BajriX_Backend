package com.example.BajriX.config;

import com.example.BajriX.Entity.Seller;

/**
 * Why this class exists:
 * Once our SessionAuthFilter verifies the seller''s "wristband" (the X-Session-Token header),
 * it stores the verified Seller here.
 *
 * How it works:
 * - Uses a ThreadLocal to hold the authenticated Seller for the duration of the current HTTP request.
 * - Any controller or service can call `CurrentSellerContext.get()` to retrieve the REAL,
 *   server-verified seller identity.
 * - This completely removes any need to trust a self-reported `sellerId` passed in the URL path.
 * - The filter automatically clears this ThreadLocal in a finally block when the request finishes,
 *   preventing thread-pool memory leaks.
 */
public class CurrentSellerContext {

    private static final ThreadLocal<Seller> currentSellerHolder = new ThreadLocal<>();

    public static void set(Seller seller) {
        currentSellerHolder.set(seller);
    }

    public static Seller get() {
        return currentSellerHolder.get();
    }

    public static Long getSellerId() {
        Seller s = currentSellerHolder.get();
        return s != null ? s.getId() : null;
    }

    public static void clear() {
        currentSellerHolder.remove();
    }
}