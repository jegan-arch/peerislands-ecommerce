package com.peerislands.ecommerce.model;

import java.util.List;

public record CreateOrderCommand(
        String customerId,
        List<OrderItemCommand> items
) {
    public record OrderItemCommand(
            String productId,
            Integer quantity
    ) {}
}