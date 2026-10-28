package com.sirji.homecare.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BranchResponse(
    UUID id,
    String name,
    String address,
    BigDecimal latitude,
    BigDecimal longitude,
    BigDecimal radiusKm,
    int priority,
    boolean active
) {}
