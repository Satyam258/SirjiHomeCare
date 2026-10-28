package com.sirji.homecare.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
    UUID id,
    String name,
    String slug,
    String category,
    String description,
    BigDecimal price,
    String unit,
    String imageUrl
) {}
