package com.sirji.homecare.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductUpsertRequest(
    @NotNull UUID categoryId,
    @NotBlank @Size(max = 180) String name,
    @NotBlank @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") @Size(max = 200) String slug,
    @Size(max = 5000) String description,
    @NotNull @DecimalMin("0.00") BigDecimal price,
    @NotBlank @Size(max = 80) String unit,
    @Size(max = 500) String imageUrl,
    boolean active
) {}
