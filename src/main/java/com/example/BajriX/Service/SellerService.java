package com.example.BajriX.Service;

import com.example.BajriX.Entity.Seller;
import com.example.BajriX.Entity.SellerStatus;
import com.example.BajriX.Repo.SellerRepo;
import com.example.BajriX.dto.SellerResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Why this service exists:
 * Handles seller profile lookups and status transitions (e.g. approving a pending seller).
 */
@Service
public class SellerService {

    private final SellerRepo sellerRepo;

    public SellerService(SellerRepo sellerRepo) {
        this.sellerRepo = sellerRepo;
    }

    public SellerResponse getSellerById(Long id) {
        Seller seller = sellerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found with id: " + id));
        return new SellerResponse(seller.getId(), seller.getName(), seller.getEmail(), seller.getStatus().name(), null, seller.getCreatedAt());
    }

    /**
     * Returns all registered sellers.
     * Useful for the frontend "Seller Switcher" dropdown so a reviewer can easily switch between
     * Shree Traders (Approved), Verma Store (Pending), and Gupta Bros (Rejected).
     */
    public List<SellerResponse> getAllSellers() {
        return sellerRepo.findAll().stream()
                .map(s -> new SellerResponse(s.getId(), s.getName(), s.getEmail(), s.getStatus().name(), null, s.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Admin/demo helper to approve or reject a seller.
     */
    @Transactional
    public SellerResponse updateStatus(Long id, SellerStatus status) {
        Seller seller = sellerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found with id: " + id));
        seller.setStatus(status);
        Seller saved = sellerRepo.save(seller);
        return new SellerResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getStatus().name(), null, saved.getCreatedAt());
    }
}