package com.peerislands.ecommerce.model;

import java.math.BigDecimal;

public record Product(
        String id,
        String name,
        BigDecimal price
) {}