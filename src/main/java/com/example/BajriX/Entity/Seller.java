package com.example.BajriX.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Why this entity exists:
 * This represents a vendor, shop, or platform administrator on BajriX.
 *
 * Security & Authorization:
 * - Each seller/admin has a unique email used for logging in.
 * - When logging in or registering, the server generates a cryptographically random
 *   opaque session token (UUID) stored in `sessionToken`.
 * - Every subsequent request provides this token via the `X-Session-Token` HTTP header.
 * - Role: ROLE_SELLER by default, or ROLE_ADMIN for platform administrators.
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

    // Role-based access control: ROLE_SELLER or ROLE_ADMIN
    @Column(nullable = false)
    private String role = "ROLE_SELLER";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Seller() {}

    public Seller(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = SellerStatus.PENDING;
        this.role = "ROLE_SELLER";
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

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}