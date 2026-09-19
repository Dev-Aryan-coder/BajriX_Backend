package com.example.BajriX.Entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Why this entity exists:
 * This is the CORE entity of the BajriX marketplace. It connects a Seller to a Product
 * and contains all commercial terms:
 * - Price per unit (e.g. ₹390 / bag)
 * - Available stock quantity (e.g. 500 bags)
 * - Minimum order quantity / MOQ (e.g. minimum 5 bags)
 * - Active flag (whether the seller currently wants to sell this item or paused it)
 * - Version (optimistic concurrency lock)
 *
 * How Concurrency Handling Works (Optimistic Locking):
 * - We have an integer field called `version` annotated with `@Version`.
 * - Suppose two shop managers (or two browser tabs) open the same listing:
 *   Manager A sees stock = 500 (version = 1).
 *   Manager B sees stock = 500 (version = 1).
 * - Manager A updates stock to 450. The database saves it and bumps version to 2.
 * - When Manager B tries to save changes with version = 1, Hibernate detects the mismatch!
 * - It immediately rejects the second update with an OptimisticLockException (translated to 409 Conflict).
 * - This prevents Manager B from silently overwriting Manager A's changes without knowing about them.
 *
 * Duplicate Prevention:
 * - We enforce `@UniqueConstraint(name = "uq_seller_product", columnNames = {"seller_id", "product_id"})`.
 * - This guarantees that a single seller cannot accidentally create two listings for the same product.
 *   If they already sell UltraTech Cement, they must edit their existing listing instead.
 */
@Entity
@Table(name = "seller_listings", uniqueConstraints = {
    @UniqueConstraint(name = "uq_seller_product", columnNames = {"seller_id", "product_id"})
})
public class SellerListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many listings can belong to one seller. We use LAZY loading so we don't fetch unnecessary data on every query.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    // Many listings can link to the same product catalogue item.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Selling price per unit. We use BigDecimal instead of double/float to avoid floating-point rounding errors in currency.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Available inventory in stock (e.g. 500 bags). Must be >= 0.
    @Column(nullable = false)
    private Integer stockQuantity;

    // Minimum Order Quantity (MOQ). Bulk construction items often have a minimum order (e.g. cannot buy just 1 brick).
    @Column(nullable = false)
    private Integer minOrderQuantity;

    // Soft delete / pause flag: if false, the seller has "stopped selling" this item.
    // We never hard-delete rows because we want to preserve historical pricing and records.
    @Column(nullable = false)
    private Boolean isActive = true;

    // Optimistic locking version number - auto-incremented by Hibernate on every UPDATE
    @Version
    private Integer version = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Automatically refresh the updatedAt timestamp whenever this row is modified
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public SellerListing() {}

    public SellerListing(Seller seller, Product product, BigDecimal price, Integer stockQuantity, Integer minOrderQuantity) {
        this.seller = seller;
        this.product = product;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.minOrderQuantity = minOrderQuantity;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Seller getSeller() { return seller; }
    public void setSeller(Seller seller) { this.seller = seller; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public Integer getMinOrderQuantity() { return minOrderQuantity; }
    public void setMinOrderQuantity(Integer minOrderQuantity) { this.minOrderQuantity = minOrderQuantity; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}