package com.sirji.homecare.repository;

import com.sirji.homecare.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByActiveTrueOrderByNameAsc();
    Optional<Category> findBySlugAndActiveTrue(String slug);
}
