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
class SellerListingAuthorizationTest {

    @Mock
    private SellerListingRepo listingRepo;

    @Mock
    private SellerRepo sellerRepo;

    @Mock
    private ProductRepo productRepo;

    private SellerListingService listingService;

    private Seller seller1; // Legitimate owner
    private Seller seller2; // Competitor / attacker
    private Product cement;
    private SellerListing listing;

    @BeforeEach
    void setUp() {
        listingService = new SellerListingService(listingRepo, sellerRepo, productRepo);

        seller1 = new Seller("Shree Traders", "shree@example.com", "hash1");
        seller1.setId(1L);

        seller2 = new Seller("Ramesh Hardware", "ramesh@example.com", "hash2");
        seller2.setId(2L);

        cement = new Product("UltraTech Cement", "Cement", "Standard 50kg bag");
        cement.setId(100L);

        listing = new SellerListing(seller1, cement, new BigDecimal("390.00"), 500, 5);
        listing.setId(50L);
        listing.setVersion(0);
    }

    @Test
    @DisplayName("Authorization Boundary: Seller 2 cannot update Seller 1's listing")
    void updateListing_otherSeller_throwsSecurityException() {
        when(listingRepo.findById(50L)).thenReturn(Optional.of(listing));

        UpdateListingRequest req = new UpdateListingRequest();
        req.setPrice(new BigDecimal("350.00"));
        req.setStockQuantity(200);
        req.setMinOrderQuantity(5);
        req.setVersion(0);

        // Caller is Seller 2 (ID: 2), but listing belongs to Seller 1 (ID: 1)
        SecurityException ex = assertThrows(SecurityException.class, () ->
                listingService.updateListing(2L, 50L, req)
        );

        assertTrue(ex.getMessage().contains("Access denied"), "Must explicitly deny unauthorized seller update");
        verify(listingRepo, never()).save(any());
    }

    @Test
    @DisplayName("Authorization Boundary: Seller 2 cannot stop or resume Seller 1's listing")
    void toggleListingActive_otherSeller_throwsSecurityException() {
        when(listingRepo.findById(50L)).thenReturn(Optional.of(listing));

        // Seller 2 tries to deactivate Seller 1's listing
        SecurityException ex = assertThrows(SecurityException.class, () ->
                listingService.toggleListingActive(2L, 50L, false)
        );

        assertTrue(ex.getMessage().contains("Access denied"));
        verify(listingRepo, never()).save(any());
    }

    @Test
    @DisplayName("Legitimate Owner: Seller 1 successfully updates own listing")
    void updateListing_owner_succeeds() {
        when(listingRepo.findById(50L)).thenReturn(Optional.of(listing));
        when(listingRepo.save(any(SellerListing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateListingRequest req = new UpdateListingRequest();
        req.setPrice(new BigDecimal("385.00"));
        req.setStockQuantity(450);
        req.setMinOrderQuantity(10);
        req.setVersion(0);

        var result = listingService.updateListing(1L, 50L, req);

        assertNotNull(result);
        assertEquals(new BigDecimal("385.00"), result.getPrice());
        assertEquals(450, result.getStockQuantity());
        verify(listingRepo).save(listing);
    }
}