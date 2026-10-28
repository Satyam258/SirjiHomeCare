package com.sirji.homecare.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AdminOrderItemResponse(
    UUID productId,
    String productName,
    int quantity,
    BigDecimal unitPrice
) {}
