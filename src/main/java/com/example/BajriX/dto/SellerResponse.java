package com.example.BajriX.dto;

import java.time.LocalDateTime;

/**
 * Response returned upon login or registration.
 * Includes the sessionToken so the frontend can store it and send it via the X-Session-Token header.
 * Also includes role (ROLE_SELLER or ROLE_ADMIN) for frontend authorization routing.
 */
public class SellerResponse {

    private Long id;
    private String name;
    private String email;
    private String status;
    private String sessionToken;
    private String role;
    private LocalDateTime createdAt;

    public SellerResponse() {}

    public SellerResponse(Long id, String name, String email, String status, String sessionToken, LocalDateTime createdAt) {
        this(id, name, email, status, sessionToken, "ROLE_SELLER", createdAt);
    }

    public SellerResponse(Long id, String name, String email, String status, String sessionToken, String role, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.status = status;
        this.sessionToken = sessionToken;
        this.role = role != null ? role : "ROLE_SELLER";
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}