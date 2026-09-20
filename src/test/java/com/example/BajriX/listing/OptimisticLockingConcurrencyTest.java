package com.example.BajriX.listing;

import com.example.BajriX.Entity.Product;
import com.example.BajriX.Entity.Seller;
import com.example.BajriX.Entity.SellerListing;
import com.example.BajriX.Repo.ProductRepo;
import com.example.BajriX.Repo.SellerListingRepo;
import com.example.BajriX.Repo.SellerRepo;
import com.example.BajriX.Service.SellerListingService;
import com.example.BajriX.dto.UpdateListingRequest;
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
class OptimisticLockingConcurrencyTest {

    @Mock
    private SellerListingRepo listingRepo;

    @Mock
    private SellerRepo sellerRepo;

    @Mock
    private ProductRepo productRepo;

    private SellerListingService listingService;

    private Seller seller;
    private Product product;
    private SellerListing listing;

    @BeforeEach
    void setUp() {
        listingService = new SellerListingService(listingRepo, sellerRepo, productRepo);

        seller = new Seller("Shree Traders", "shree@example.com", "hash");
        seller.setId(1L);

        product = new Product("UltraTech Cement", "Cement", "50kg");
        product.setId(100L);

        listing = new SellerListing(seller, product, new BigDecimal("390.00"), 500, 5);
        listing.setId(10L);
        // Entity is currently at version 2 in the database
        listing.setVersion(2);
    }

    @Test
    @DisplayName("Concurrency: rejects update when client version is stale (optimistic lock conflict)")
    void updateListing_versionMismatch_throwsConflictException() {
        when(listingRepo.findById(10L)).thenReturn(Optional.of(listing));

        UpdateListingRequest req = new UpdateListingRequest();
        req.setPrice(new BigDecimal("395.00"));
        req.setStockQuantity(400);
        req.setMinOrderQuantity(5);
        // Client still has stale version 1 (another user or tab updated to version 2 in the meantime)
        req.setVersion(1);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                listingService.updateListing(1L, 10L, req)
        );

        assertTrue(ex.getMessage().contains("Conflict detected"), "Must detect stale version conflict");
        assertTrue(ex.getMessage().contains("updated by another process"));
        verify(listingRepo, never()).save(any());
    }

    @Test
    @DisplayName("Concurrency: accepts update when client version matches current entity version")
    void updateListing_matchingVersion_succeeds() {
        when(listingRepo.findById(10L)).thenReturn(Optional.of(listing));
        when(listingRepo.save(any(SellerListing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateListingRequest req = new UpdateListingRequest();
        req.setPrice(new BigDecimal("395.00"));
        req.setStockQuantity(400);
        req.setMinOrderQuantity(5);
        // Matching version 2
        req.setVersion(2);

        var response = listingService.updateListing(1L, 10L, req);

        assertNotNull(response);
        assertEquals(new BigDecimal("395.00"), response.getPrice());
        assertEquals(400, response.getStockQuantity());
        verify(listingRepo).save(listing);
    }
}