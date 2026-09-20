package com.example.BajriX.product;

import com.example.BajriX.Entity.Product;
import com.example.BajriX.Entity.Seller;
import com.example.BajriX.Entity.SellerListing;
import com.example.BajriX.Entity.SellerStatus;
import com.example.BajriX.Repo.ProductRepo;
import com.example.BajriX.Repo.SellerListingRepo;
import com.example.BajriX.Service.ProductService;
import com.example.BajriX.dto.ProductDetailResponse;
import com.example.BajriX.dto.ProductSummaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductStatusFilteringTest {

    @Mock
    private ProductRepo productRepo;

    @Mock
    private SellerListingRepo listingRepo;

    private ProductService productService;

    private Product cement;
    private Seller approvedSeller1;
    private Seller approvedSeller2;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepo, listingRepo);

        cement = new Product("UltraTech Cement", "Cement", "Standard 50kg bag");
        cement.setId(100L);

        approvedSeller1 = new Seller("Ramesh Hardware", "ramesh@example.com", "h1");
        approvedSeller1.setId(1L);
        approvedSeller1.setStatus(SellerStatus.APPROVED);

        approvedSeller2 = new Seller("Shree Traders", "shree@example.com", "h2");
        approvedSeller2.setId(2L);
        approvedSeller2.setStatus(SellerStatus.APPROVED);
    }

    @Test
    @DisplayName("Buyer Search: strictly queries APPROVED sellers and calculates lowest starting price")
    void searchProducts_onlyQueriesApprovedSellers() {
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepo.searchProducts(null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(cement)));

        SellerListing listing1 = new SellerListing(approvedSeller1, cement, new BigDecimal("385.00"), 200, 10);
        listing1.setId(1L);
        SellerListing listing2 = new SellerListing(approvedSeller2, cement, new BigDecimal("390.00"), 500, 5);
        listing2.setId(2L);

        // Crucial test: verify repository is queried specifically with SellerStatus.APPROVED
        when(listingRepo.findActiveApprovedListingsForProduct(100L, SellerStatus.APPROVED))
                .thenReturn(List.of(listing1, listing2));

        var page = productService.searchProducts(null, null, pageable);

        assertNotNull(page);
        assertEquals(1, page.getTotalElements());
        ProductSummaryResponse summary = page.getContent().get(0);

        assertEquals("UltraTech Cement", summary.getName());
        assertEquals(new BigDecimal("385.00"), summary.getLowestPrice(), "Lowest price must be ₹385 from approved seller");
        assertEquals(2, summary.getSellerCount(), "Must report 2 approved sellers");

        verify(listingRepo).findActiveApprovedListingsForProduct(100L, SellerStatus.APPROVED);
    }

    @Test
    @DisplayName("Buyer Detail View: only returns active approved seller listings")
    void getProductDetail_returnsOnlyApprovedListings() {
        when(productRepo.findById(100L)).thenReturn(Optional.of(cement));

        SellerListing listing1 = new SellerListing(approvedSeller1, cement, new BigDecimal("385.00"), 200, 10);
        listing1.setId(1L);
        listing1.setVersion(0);

        when(listingRepo.findActiveApprovedListingsForProduct(100L, SellerStatus.APPROVED))
                .thenReturn(List.of(listing1));

        ProductDetailResponse detail = productService.getProductDetail(100L);

        assertNotNull(detail);
        assertEquals(100L, detail.getId());
        assertEquals(1, detail.getSellerListings().size());
        assertEquals("Ramesh Hardware", detail.getSellerListings().get(0).getSellerName());
        assertEquals("APPROVED", detail.getSellerListings().get(0).getSellerStatus());

        verify(listingRepo).findActiveApprovedListingsForProduct(100L, SellerStatus.APPROVED);
    }
}