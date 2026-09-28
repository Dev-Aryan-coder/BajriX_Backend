package com.example.BajriX.dto;

public class AdminDashboardStatsResponse {
    private long totalProducts;
    private long totalListings;
    private long totalSellers;

    public AdminDashboardStatsResponse() {}

    public AdminDashboardStatsResponse(long totalProducts, long totalListings, long totalSellers) {
        this.totalProducts = totalProducts;
        this.totalListings = totalListings;
        this.totalSellers = totalSellers;
    }

    public long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(long totalProducts) { this.totalProducts = totalProducts; }

    public long getTotalListings() { return totalListings; }
    public void setTotalListings(long totalListings) { this.totalListings = totalListings; }

    public long getTotalSellers() { return totalSellers; }
    public void setTotalSellers(long totalSellers) { this.totalSellers = totalSellers; }
}
