package com.example.BajriX.listing;

import com.example.BajriX.Entity.Product;
import com.example.BajriX.Entity.Seller;
import com.example.BajriX.Entity.SellerListing;
import com.example.BajriX.Repo.ProductRepo;
import com.example.BajriX.Repo.SellerListingRepo;
import com.example.BajriX.Repo.SellerRepo;
import com.example.BajriX.Service.SellerListingService;
import com.example.BajriX.dto.CreateListingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DuplicateListingTest {

    @Mock
    private SellerListingRepo listingRepo;

    @Mock
    private SellerRepo sellerRepo;

    @Mock
    private ProductRepo productRepo;

    private SellerListingService listingService;

    private Seller seller;
    private Product product;

    @BeforeEach
    void setUp() {
        listingService = new SellerListingService(listingRepo, sellerRepo, productRepo);

        seller = new Seller("Shree Traders", "shree@example.com", "h1");
        seller.setId(1L);

        product = new Product("UltraTech Cement", "Cement", "50kg");
        product.setId(100L);
    }

    @Test
    @DisplayName("Business Rule: rejects duplicate listing when seller already sells this product")
    void createListing_duplicateProduct_throwsIllegalStateException() {
        when(sellerRepo.findById(1L)).thenReturn(Optional.of(seller));
        when(productRepo.findById(100L)).thenReturn(Optional.of(product));

        // Seller 1 ALREADY has a listing for Product 100
        when(listingRepo.existsBySellerIdAndProductId(1L, 100L)).thenReturn(true);

        CreateListingRequest req = new CreateListingRequest();
        req.setProductId(100L);
        req.setPrice(new BigDecimal("390.00"));
        req.setStockQuantity(500);
        req.setMinOrderQuantity(10);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                listingService.createListing(1L, req)
        );

        assertTrue(ex.getMessage().contains("already have a listing for this product"));
        verify(listingRepo, never()).save(any(SellerListing.class));
    }

    @Test
    @DisplayName("Business Rule: rejects listing when MOQ exceeds available stock quantity")
    void createListing_moqExceedsStock_throwsIllegalArgumentException() {
        when(sellerRepo.findById(1L)).thenReturn(Optional.of(seller));
        when(productRepo.findById(100L)).thenReturn(Optional.of(product));
        when(listingRepo.existsBySellerIdAndProductId(1L, 100L)).thenReturn(false);

        CreateListingRequest req = new CreateListingRequest();
        req.setProductId(100L);
        req.setPrice(new BigDecimal("390.00"));
        req.setStockQuantity(50);
        // MOQ 100 is higher than available stock 50
        req.setMinOrderQuantity(100);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                listingService.createListing(1L, req)
        );

        assertTrue(ex.getMessage().contains("cannot be greater than available stock"));
        verify(listingRepo, never()).save(any(SellerListing.class));
    }
}