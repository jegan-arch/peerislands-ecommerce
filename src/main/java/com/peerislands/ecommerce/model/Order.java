package com.peerislands.ecommerce.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record Order(
        UUID id,
        String customerId,
        OrderStatus status,
        List<OrderItem> items,
        LocalDateTime createdAt
) {}