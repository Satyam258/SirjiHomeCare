package com.sirji.homecare.web.dto;

import com.sirji.homecare.domain.OrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminOrderResponse(
    UUID id,
    String orderNumber,
    String customerName,
    String customerPhone,
    String deliveryAddress,
    OrderStatus status,
    boolean bulkOrder,
    String branchName,
    String adminNotes,
    Instant createdAt,
    List<AdminOrderItemResponse> items
) {}
