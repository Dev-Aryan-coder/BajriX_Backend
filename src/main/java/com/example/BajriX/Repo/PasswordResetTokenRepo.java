package com.example.BajriX.Repo;

import com.example.BajriX.Entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Why this repository exists:
 * Allows looking up reset tokens when a seller clicks a password reset link.
 */
@Repository
public interface PasswordResetTokenRepo extends JpaRepository<PasswordResetToken, Long> {
    // Looks up the token record to check expiry and used state
    Optional<PasswordResetToken> findByToken(String token);
}