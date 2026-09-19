package com.example.BajriX.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Why this entity exists:
 * This represents a vendor or shop selling construction materials on BajriX (e.g. "Shree Traders").
 *
 * Security & Authorization:
 * - Each seller has a unique email used for logging in.
 * - When a seller logs in or registers, the server generates a cryptographically random
 *   opaque session token (UUID) stored in `sessionToken`.
 * - Every subsequent seller-scoped write/read request MUST provide this token via the
 *   `X-Session-Token` HTTP header.
 * - The backend resolves the seller strictly from this token — preventing any attacker
 *   from simply swapping the sellerId in the URL path to modify someone else''s data.
 */
@Entity
@Table(name = "sellers")
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    // Opaque session token issued upon login/register for stateless yet secure authorization
    @Column(name = "session_token", unique = true)
    private String sessionToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SellerStatus status = SellerStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Seller() {}

    public Seller(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = SellerStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public SellerStatus getStatus() { return status; }
    public void setStatus(SellerStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}