package com.peerislands.ecommerce.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(UUID id, String customerId, String status, List<OrderItemDto> items, LocalDateTime createdAt) {}
