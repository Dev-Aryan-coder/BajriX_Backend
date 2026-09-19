package com.example.BajriX.Controller;

import com.example.BajriX.Entity.SellerStatus;
import com.example.BajriX.Service.SellerService;
import com.example.BajriX.dto.ApiResponse;
import com.example.BajriX.dto.SellerResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// seller profile & admin/demo status management
@RestController
@RequestMapping("/api/v1/sellers")
public class SellerController {

    private final SellerService sellerService;

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SellerResponse>> getSeller(@PathVariable Long id) {
        SellerResponse seller = sellerService.getSellerById(id);
        return ResponseEntity.ok(ApiResponse.ok("Seller details", seller));
    }

    // list all sellers (handy for seller-switcher in demo)
    @GetMapping
    public ResponseEntity<ApiResponse<List<SellerResponse>>> getAllSellers() {
        List<SellerResponse> sellers = sellerService.getAllSellers();
        return ResponseEntity.ok(ApiResponse.ok("All sellers", sellers));
    }

    // demo endpoint to approve/reject sellers
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SellerResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam SellerStatus status
    ) {
        SellerResponse updated = sellerService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Seller status changed to " + status, updated));
    }
}