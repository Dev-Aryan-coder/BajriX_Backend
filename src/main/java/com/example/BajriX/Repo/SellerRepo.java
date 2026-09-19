package com.example.BajriX.Repo;

import com.example.BajriX.Entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Why this repository exists:
 * Manages database access for seller accounts and authenticates requests via session tokens.
 */
@Repository
public interface SellerRepo extends JpaRepository<Seller, Long> {
    Optional<Seller> findByEmail(String email);
    boolean existsByEmail(String email);

    // Resolves the authenticated seller from the X-Session-Token header
    Optional<Seller> findBySessionToken(String sessionToken);
}