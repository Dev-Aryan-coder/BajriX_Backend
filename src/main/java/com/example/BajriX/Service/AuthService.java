package com.example.BajriX.Service;

import com.example.BajriX.Entity.PasswordResetToken;
import com.example.BajriX.Entity.Seller;
import com.example.BajriX.Entity.SellerStatus;
import com.example.BajriX.Repo.PasswordResetTokenRepo;
import com.example.BajriX.Repo.SellerRepo;
import com.example.BajriX.dto.LoginRequest;
import com.example.BajriX.dto.RegisterRequest;
import com.example.BajriX.dto.SellerResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Why this service exists:
 * Manages seller registration, authentication, token issuance, and server-side token resolution.
 *
 * Real Authorization Boundary Implementation:
 * 1. On login or register, we generate an unforgeable opaque token: `UUID.randomUUID().toString()`.
 * 2. We store this token against the seller in MySQL.
 * 3. The client receives this token and passes it in the `X-Session-Token` header on all protected requests.
 * 4. `resolveSellerByToken`: Server-side lookup that verifies the token. If invalid or missing,
 *    it throws a `SecurityException` (403 Forbidden).
 *    This completely eliminates the vulnerability where a caller could tamper with the URL sellerId.
 */
@Service
public class AuthService {

    private final SellerRepo sellerRepo;
    private final PasswordResetTokenRepo tokenRepo;

    public AuthService(SellerRepo sellerRepo, PasswordResetTokenRepo tokenRepo) {
        this.sellerRepo = sellerRepo;
        this.tokenRepo = tokenRepo;
    }

    /**
     * Registers a new seller account.
     * Generates a new session token so the seller is immediately logged in upon registration.
     */
    @Transactional
    public SellerResponse register(RegisterRequest req) {
        if (sellerRepo.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("This email is already registered. Please log in instead.");
        }

        Seller seller = new Seller();
        seller.setName(req.getName());
        seller.setEmail(req.getEmail().toLowerCase().trim());
        seller.setPasswordHash(req.getPassword());
        seller.setStatus(SellerStatus.PENDING);
        seller.setCreatedAt(LocalDateTime.now());

        // Issue fresh session token
        seller.setSessionToken(UUID.randomUUID().toString());

        Seller saved = sellerRepo.save(seller);
        return mapToResponse(saved);
    }

    /**
     * Validates credentials and issues a fresh session token.
     */
    @Transactional
    public SellerResponse login(LoginRequest req) {
        Seller seller = sellerRepo.findByEmail(req.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("Incorrect email or password"));

        if (!seller.getPasswordHash().equals(req.getPassword())) {
            throw new IllegalArgumentException("Incorrect email or password");
        }

        // Issue fresh session token upon each successful login
        seller.setSessionToken(UUID.randomUUID().toString());
        Seller updated = sellerRepo.save(seller);

        return mapToResponse(updated);
    }

    /**
     * CORE SECURITY METHOD:
     * Resolves the real seller identity from the X-Session-Token header.
     * NEVER trusts user-supplied seller IDs in path variables.
     */
    public Seller resolveSellerByToken(String sessionToken) {
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            throw new SecurityException("Authentication required: Missing 'X-Session-Token' header. Please log in.");
        }

        return sellerRepo.findBySessionToken(sessionToken.trim())
                .orElseThrow(() -> new SecurityException("Authentication failed: Invalid or expired session token. Please log in again."));
    }

    @Transactional
    public String requestPasswordReset(String email) {
        Seller seller = sellerRepo.findByEmail(email.toLowerCase().trim()).orElse(null);

        if (seller != null) {
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(seller, token, LocalDateTime.now().plusMinutes(30));
            tokenRepo.save(resetToken);
            return token;
        }
        return null;
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link."));

        if (resetToken.getUsed() || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("This reset link has expired or has already been used. Please request a new one.");
        }

        Seller seller = resetToken.getSeller();
        seller.setPasswordHash(newPassword);
        // Invalidate old session token on password change for security
        seller.setSessionToken(UUID.randomUUID().toString());
        sellerRepo.save(seller);

        resetToken.setUsed(true);
        tokenRepo.save(resetToken);
    }

    private SellerResponse mapToResponse(Seller seller) {
        return new SellerResponse(
                seller.getId(),
                seller.getName(),
                seller.getEmail(),
                seller.getStatus().name(),
                seller.getSessionToken(),
                seller.getCreatedAt()
        );
    }
}