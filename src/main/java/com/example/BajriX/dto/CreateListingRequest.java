package com.example.BajriX.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

// request to add a product listing (links to existing product or provides new product details)
public class CreateListingRequest {

    // if linking to existing product in catalogue
    private Long productId;

    // or if creating a new product directly
    private String newProductName;
    private String newProductCategory;
    private String newProductDescription;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stockQuantity;

    @NotNull(message = "Minimum order quantity is required")
    @Min(value = 1, message = "MOQ must be at least 1")
    private Integer minOrderQuantity;

    public CreateListingRequest() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getNewProductName() { return newProductName; }
    public void setNewProductName(String newProductName) { this.newProductName = newProductName; }

    public String getNewProductCategory() { return newProductCategory; }
    public void setNewProductCategory(String newProductCategory) { this.newProductCategory = newProductCategory; }

    public String getNewProductDescription() { return newProductDescription; }
    public void setNewProductDescription(String newProductDescription) { this.newProductDescription = newProductDescription; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public Integer getMinOrderQuantity() { return minOrderQuantity; }
    public void setMinOrderQuantity(Integer minOrderQuantity) { this.minOrderQuantity = minOrderQuantity; }
}