package com.example.BajriX.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Why this entity exists:
 * In a multi-seller marketplace, there is a fundamental distinction between:
 * 1. The PRODUCT: What is being sold (the shared master catalogue item, e.g. "UltraTech PPC Cement 50kg").
 * 2. The SELLER LISTING: Who is selling it, for how much, and how many they have in stock.
 *
 * If every seller created their own independent product entry, buyers would see 20 different cards
 * for "UltraTech Cement" with slightly different names, making price comparison impossible.
 *
 * By having this centralized Product entity:
 * - A single master product exists once in the database.
 * - Multiple sellers attach their listings to this same product ID.
 * - Buyers can open this single product and see a side-by-side comparison table of all sellers offering it.
 */
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The official product name (e.g., "UltraTech PPC Cement 50kg")
    @Column(nullable = false)
    private String name;

    // Category for grouping and filtering (e.g., "Cement", "Bricks", "Steel", "Aggregates")
    @Column(nullable = false)
    private String category;

    // Technical specifications or details (e.g., "Grade 53, suitable for RCC work")
    @Column(columnDefinition = "TEXT")
    private String description;

    // When this catalogue item was originally created in the platform
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Default constructor required by JPA
    public Product() {}

    // Convenience constructor for adding new catalogue items
    public Product(String name, String category, String description) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}