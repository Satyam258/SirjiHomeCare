package com.sirji.homecare.web.dto;

import com.sirji.homecare.domain.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    String orderNumber,
    OrderStatus status,
    boolean bulkOrder,
    String branchName,
    Instant createdAt
) {}
