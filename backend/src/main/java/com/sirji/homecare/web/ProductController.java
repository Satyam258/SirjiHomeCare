package com.sirji.homecare.web;

import com.sirji.homecare.service.ProductService;
import com.sirji.homecare.web.dto.ProductResponse;
import com.sirji.homecare.web.dto.ProductUpsertRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public List<ProductResponse> listProducts() {
        return productService.listActive();
    }

    @GetMapping("/products/{slug}")
    public ProductResponse getProduct(@PathVariable String slug) {
        return productService.findBySlug(slug);
    }

    @PostMapping("/admin/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody ProductUpsertRequest request) {
        return productService.create(request);
    }

    @PutMapping("/admin/products/{id}")
    public ProductResponse updateProduct(@PathVariable UUID id, @Valid @RequestBody ProductUpsertRequest request) {
        return productService.update(id, request);
    }
}
