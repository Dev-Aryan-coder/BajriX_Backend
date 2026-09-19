package com.example.BajriX.dto;

import java.math.BigDecimal;

// for buyer list view: shows product with lowest available price and seller count
public class ProductSummaryResponse {

    private Long id;
    private String name;
    private String category;
    private String description;
    private BigDecimal lowestPrice;
    private Integer sellerCount;

    public ProductSummaryResponse() {}

    public ProductSummaryResponse(Long id, String name, String category, String description, BigDecimal lowestPrice, Integer sellerCount) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.lowestPrice = lowestPrice;
        this.sellerCount = sellerCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getLowestPrice() { return lowestPrice; }
    public void setLowestPrice(BigDecimal lowestPrice) { this.lowestPrice = lowestPrice; }

    public Integer getSellerCount() { return sellerCount; }
    public void setSellerCount(Integer sellerCount) { this.sellerCount = sellerCount; }
}