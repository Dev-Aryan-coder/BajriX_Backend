package com.example.BajriX.Repo;

import com.example.BajriX.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepo extends JpaRepository<Product, Long> {
    // Basic CRUD and count() are inherited from JpaRepository
}
