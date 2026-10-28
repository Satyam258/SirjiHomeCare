package com.sirji.homecare.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateOrderItemRequest(
    @NotNull UUID productId,
    @Min(1) @Max(10000) int quantity
) {}
