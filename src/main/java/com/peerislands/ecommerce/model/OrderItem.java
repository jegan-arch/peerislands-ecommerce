package com.peerislands.ecommerce.model;

import java.math.BigDecimal;

public record OrderItem(
        String productId,
        Integer quantity,
        BigDecimal price
) {}