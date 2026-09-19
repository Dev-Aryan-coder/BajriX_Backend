package com.example.BajriX.Repo;

import com.example.BajriX.Entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Why this repository exists:
 * Handles catalogue product searching and filtering for buyers and sellers.
 *
 * How it works:
 * - `searchProducts`: Allows buyers to search by typing a keyword (e.g. "cement") and/or selecting
 *   a category (e.g. "Cement", "Bricks").
 *   - Case-insensitive matching via LOWER().
 *   - Supports pagination so we never dump millions of rows into memory at once.
 * - `findTop10ByNameContainingIgnoreCase`: A lightweight autocomplete query used by sellers when
 *   adding a product, so they can quickly check if a product already exists before creating a duplicate.
 */
@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {

    // Main search query for buyer browsing.
    // If category is null or empty, it ignores the category filter.
    // If search keyword is null or empty, it returns all products in that category.
    @Query("SELECT p FROM Product p WHERE " +
           "(:category IS NULL OR :category = '' OR LOWER(p.category) = LOWER(:category)) AND " +
           "(:search IS NULL OR :search = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> searchProducts(@Param("search") String search, @Param("category") String category, Pageable pageable);

    // Autocomplete lookup for the "Add Product" seller workflow.
    // Returns up to 10 matching products to prevent UI clutter.
    List<Product> findTop10ByNameContainingIgnoreCase(String name);
}