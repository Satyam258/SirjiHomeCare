package com.sirji.homecare.repository;

import com.sirji.homecare.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByActiveTrueOrderByNameAsc();
    Optional<Product> findByIdAndActiveTrue(UUID id);
    Optional<Product> findBySlugAndActiveTrue(String slug);
}
