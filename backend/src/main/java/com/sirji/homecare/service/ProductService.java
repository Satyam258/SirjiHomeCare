package com.sirji.homecare.service;

import com.sirji.homecare.domain.Category;
import com.sirji.homecare.domain.Product;
import com.sirji.homecare.repository.CategoryRepository;
import com.sirji.homecare.repository.ProductRepository;
import com.sirji.homecare.web.dto.ProductResponse;
import com.sirji.homecare.web.dto.ProductUpsertRequest;
import com.sirji.homecare.web.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listActive() {
        return productRepository.findByActiveTrueOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findBySlug(String slug) {
        return productRepository.findBySlugAndActiveTrue(slug)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
    }

    @Transactional
    public ProductResponse create(ProductUpsertRequest request) {
        Product product = new Product();
        apply(product, request);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(UUID id, ProductUpsertRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
        apply(product, request);
        return toResponse(productRepository.save(product));
    }

    private void apply(Product product, ProductUpsertRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
            .filter(Category::isActive)
            .orElseThrow(() -> new ResourceNotFoundException("Active category not found."));
        product.setCategory(category);
        product.setName(request.name().trim());
        product.setSlug(request.slug().trim());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setUnit(request.unit().trim());
        product.setImageUrl(request.imageUrl());
        product.setActive(request.active());
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getSlug(), product.getCategory().getName(),
            product.getDescription(), product.getPrice(), product.getUnit(), product.getImageUrl());
    }
}
