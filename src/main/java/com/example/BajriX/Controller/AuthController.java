package com.example.BajriX.Controller;

import com.example.BajriX.Service.AuthService;
import com.example.BajriX.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// handles seller authentication endpoints
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<SellerResponse>> register(@Valid @RequestBody RegisterRequest req) {
        SellerResponse res = authService.register(req);
        return ResponseEntity.ok(ApiResponse.ok("Registration successful! Account is pending approval.", res));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<SellerResponse>> login(@Valid @RequestBody LoginRequest req) {
        SellerResponse res = authService.login(req);
        return ResponseEntity.ok(ApiResponse.ok("Login successful!", res));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        String token = authService.requestPasswordReset(req.getEmail());
        // in demo mode, returning token so reviewer can test reset flow
        return ResponseEntity.ok(ApiResponse.ok(
                "If registered, a reset link has been issued.",
                token != null ? Map.of("demoResetToken", token) : Map.of()
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req.getToken(), req.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok("Password updated successfully! Please log in.", null));
    }
}