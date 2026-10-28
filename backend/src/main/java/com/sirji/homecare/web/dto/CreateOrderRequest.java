package com.sirji.homecare.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
    @NotBlank @Size(max = 160) String customerName,
    @NotBlank @Pattern(regexp = "[0-9+() .-]{8,30}") String customerPhone,
    @NotBlank @Size(max = 2000) String deliveryAddress,
    @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude,
    @NotEmpty @Size(max = 100) List<@Valid CreateOrderItemRequest> items
) {}
