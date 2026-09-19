package com.example.BajriX.config;

import com.example.BajriX.Entity.Product;
import com.example.BajriX.Entity.Seller;
import com.example.BajriX.Entity.SellerListing;
import com.example.BajriX.Entity.SellerStatus;
import com.example.BajriX.Repo.ProductRepo;
import com.example.BajriX.Repo.SellerListingRepo;
import com.example.BajriX.Repo.SellerRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Seeds realistic sample data on startup.
 *
 * For seamless testing of our X-Session-Token authorization boundary:
 * Each seeded seller is assigned a well-known demo session token:
 * - Shree Traders: token-shree-123
 * - Ramesh Hardware: token-ramesh-456
 * - BuildWell Supplies: token-buildwell-789
 * - Verma Store (PENDING): token-verma-999
 * - Gupta Bros (REJECTED): token-gupta-000
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductRepo productRepo;
    private final SellerRepo sellerRepo;
    private final SellerListingRepo listingRepo;

    public DataInitializer(ProductRepo productRepo, SellerRepo sellerRepo, SellerListingRepo listingRepo) {
        this.productRepo = productRepo;
        this.sellerRepo = sellerRepo;
        this.listingRepo = listingRepo;
    }

    @Override
    public void run(String... args) {
        if (productRepo.count() > 0) {
            return;
        }

        // 1. Create Sellers with demo session tokens
        Seller shree = new Seller("Shree Traders", "shree@example.com", "password123");
        shree.setStatus(SellerStatus.APPROVED);
        shree.setSessionToken("token-shree-123");
        sellerRepo.save(shree);

        Seller ramesh = new Seller("Ramesh Hardware", "ramesh@example.com", "password123");
        ramesh.setStatus(SellerStatus.APPROVED);
        ramesh.setSessionToken("token-ramesh-456");
        sellerRepo.save(ramesh);

        Seller buildwell = new Seller("BuildWell Supplies", "buildwell@example.com", "password123");
        buildwell.setStatus(SellerStatus.APPROVED);
        buildwell.setSessionToken("token-buildwell-789");
        sellerRepo.save(buildwell);

        Seller verma = new Seller("Verma Cement Store", "verma@example.com", "password123");
        verma.setStatus(SellerStatus.PENDING);
        verma.setSessionToken("token-verma-999");
        sellerRepo.save(verma);

        Seller gupta = new Seller("Gupta Bros Materials", "gupta@example.com", "password123");
        gupta.setStatus(SellerStatus.REJECTED);
        gupta.setSessionToken("token-gupta-000");
        sellerRepo.save(gupta);

        // 2. Create Products
        Product cement = new Product(
                "UltraTech PPC Cement 50kg",
                "Cement",
                "Premium Portland Pozzolana Cement for heavy-duty structural concrete and home building."
        );
        productRepo.save(cement);

        Product bricks = new Product(
                "Red Clay Construction Bricks",
                "Bricks",
                "Kiln-fired high-density red clay bricks for masonry and foundation walls."
        );
        productRepo.save(bricks);

        Product steel = new Product(
                "Tata Tiscon 550D TMT Rebar 12mm",
                "Steel",
                "High-strength earthquake-resistant thermo-mechanically treated reinforcement bars."
        );
        productRepo.save(steel);

        Product sand = new Product(
                "River Sand and Coarse Aggregate per Ton",
                "Aggregates",
                "Washed natural river sand suitable for plastering and concrete mixing."
        );
        productRepo.save(sand);

        // 3. Create Seller Listings for UltraTech Cement (Demonstrates multi-seller comparison)
        SellerListing list1 = new SellerListing(shree, cement, new BigDecimal("390.00"), 500, 5);
        listingRepo.save(list1);

        SellerListing list2 = new SellerListing(ramesh, cement, new BigDecimal("385.00"), 200, 10);
        listingRepo.save(list2);

        SellerListing list3 = new SellerListing(buildwell, cement, new BigDecimal("405.00"), 1000, 20);
        listingRepo.save(list3);

        // Verma (PENDING seller) listing - excluded from buyer views
        SellerListing list4 = new SellerListing(verma, cement, new BigDecimal("380.00"), 150, 5);
        listingRepo.save(list4);

        // 4. Create Listings for other products
        SellerListing list5 = new SellerListing(shree, bricks, new BigDecimal("9.50"), 10000, 500);
        listingRepo.save(list5);

        SellerListing list6 = new SellerListing(ramesh, bricks, new BigDecimal("9.20"), 4000, 1000);
        listingRepo.save(list6);

        SellerListing list7 = new SellerListing(buildwell, steel, new BigDecimal("62000.00"), 50, 1);
        listingRepo.save(list7);

        SellerListing list8 = new SellerListing(shree, sand, new BigDecimal("1850.00"), 80, 2);
        listingRepo.save(list8);
    }
}