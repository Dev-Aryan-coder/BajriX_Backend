package com.example.BajriX.dto;

import java.util.List;

// for buyer detail view: shows product details and all active seller listings side-by-side
public class ProductDetailResponse {

    private Long id;
    private String name;
    private String category;
    private String description;
    private List<ListingResponse> sellerListings;

    public ProductDetailResponse() {}

    public ProductDetailResponse(Long id, String name, String category, String description, List<ListingResponse> sellerListings) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.sellerListings = sellerListings;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<ListingResponse> getSellerListings() { return sellerListings; }
    public void setSellerListings(List<ListingResponse> sellerListings) { this.sellerListings = sellerListings; }
}