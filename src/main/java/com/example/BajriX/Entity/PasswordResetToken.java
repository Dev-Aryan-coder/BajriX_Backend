package com.example.BajriX.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Why this entity exists:
 * This handles the "Forgot Password" flow cleanly and safely.
 *
 * How it works:
 * 1. When a seller enters their email on the "Forgot Password" screen, we generate a random,
 *    unpredictable UUID token.
 * 2. We store the token in this table with an expiry timestamp (e.g. 30 minutes from now).
 * 3. In production, this link is emailed. In our demo mode, we return/log the link so
 *    anyone testing the system can verify the password reset flow.
 * 4. When the seller submits a new password using this token, we verify that:
 *    - The token exists.
 *    - It hasn't expired.
 *    - It hasn't already been used (`used = false`).
 * 5. Once used, `used` is set to true so the same link cannot be replayed.
 */
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The seller requesting the password reset
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    // Secure random UUID token
    @Column(nullable = false, unique = true)
    private String token;

    // Timestamp after which this token is no longer valid
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    // Prevents replay attacks - once used to change password, cannot be reused
    @Column(nullable = false)
    private Boolean used = false;

    public PasswordResetToken() {}

    public PasswordResetToken(Seller seller, String token, LocalDateTime expiresAt) {
        this.seller = seller;
        this.token = token;
        this.expiresAt = expiresAt;
        this.used = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Seller getSeller() { return seller; }
    public void setSeller(Seller seller) { this.seller = seller; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public Boolean getUsed() { return used; }
    public void setUsed(Boolean used) { this.used = used; }
}