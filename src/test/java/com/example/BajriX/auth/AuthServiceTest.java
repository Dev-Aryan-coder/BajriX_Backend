package com.example.BajriX.auth;

import com.example.BajriX.Entity.Seller;
import com.example.BajriX.Entity.SellerStatus;
import com.example.BajriX.Repo.PasswordResetTokenRepo;
import com.example.BajriX.Repo.SellerRepo;
import com.example.BajriX.Service.AuthService;
import com.example.BajriX.dto.LoginRequest;
import com.example.BajriX.dto.RegisterRequest;
import com.example.BajriX.dto.SellerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private SellerRepo sellerRepo;

    @Mock
    private PasswordResetTokenRepo tokenRepo;

    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(sellerRepo, tokenRepo, passwordEncoder);
    }

    @Test
    @DisplayName("Registration: hashes password with BCrypt and issues session token")
    void register_hashesPasswordWithBCrypt() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Patel Hardware");
        req.setEmail("patel@example.com");
        req.setPassword("securePass123");

        when(sellerRepo.existsByEmail("patel@example.com")).thenReturn(false);
        when(sellerRepo.save(any(Seller.class))).thenAnswer(invocation -> {
            Seller s = invocation.getArgument(0);
            s.setId(10L);
            return s;
        });

        SellerResponse res = authService.register(req);

        assertNotNull(res);
        assertEquals("Patel Hardware", res.getName());
        assertEquals("PENDING", res.getStatus());
        assertNotNull(res.getSessionToken(), "Session wristband token must be issued on registration");

        // Verify password was hashed and NOT saved as plaintext
        ArgumentCaptor<Seller> captor = ArgumentCaptor.forClass(Seller.class);
        verify(sellerRepo).save(captor.capture());
        Seller saved = captor.getValue();

        assertNotEquals("securePass123", saved.getPasswordHash(), "Plaintext password must never be persisted");
        assertTrue(passwordEncoder.matches("securePass123", saved.getPasswordHash()), "Hash must match raw password with BCrypt");
    }

    @Test
    @DisplayName("Login: succeeds with valid BCrypt password and rotates session token")
    void login_successWithBCryptPassword() {
        String hashedPassword = passwordEncoder.encode("myPassword123");
        Seller seller = new Seller("Patel Hardware", "patel@example.com", hashedPassword);
        seller.setId(10L);
        seller.setStatus(SellerStatus.APPROVED);
        seller.setSessionToken("old-token-111");

        when(sellerRepo.findByEmail("patel@example.com")).thenReturn(Optional.of(seller));
        when(sellerRepo.save(any(Seller.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoginRequest req = new LoginRequest();
        req.setEmail("patel@example.com");
        req.setPassword("myPassword123");

        SellerResponse res = authService.login(req);

        assertNotNull(res);
        assertNotNull(res.getSessionToken());
        assertNotEquals("old-token-111", res.getSessionToken(), "Fresh session token must be issued upon login");
    }

    @Test
    @DisplayName("Login: rejects invalid password with clear error message")
    void login_wrongPassword_throwsException() {
        String hashedPassword = passwordEncoder.encode("correctPassword");
        Seller seller = new Seller("Patel Hardware", "patel@example.com", hashedPassword);
        seller.setId(10L);

        when(sellerRepo.findByEmail("patel@example.com")).thenReturn(Optional.of(seller));

        LoginRequest req = new LoginRequest();
        req.setEmail("patel@example.com");
        req.setPassword("wrongPassword");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.login(req));
        assertEquals("Incorrect email or password", ex.getMessage());
    }

    @Test
    @DisplayName("Authorization Boundary: resolveSellerByToken rejects missing or unknown token")
    void resolveSellerByToken_invalidToken_throwsSecurityException() {
        // Missing token
        assertThrows(SecurityException.class, () -> authService.resolveSellerByToken(null));
        assertThrows(SecurityException.class, () -> authService.resolveSellerByToken("   "));

        // Unknown token
        when(sellerRepo.findBySessionToken("forged-fake-token")).thenReturn(Optional.empty());
        assertThrows(SecurityException.class, () -> authService.resolveSellerByToken("forged-fake-token"));
    }
}