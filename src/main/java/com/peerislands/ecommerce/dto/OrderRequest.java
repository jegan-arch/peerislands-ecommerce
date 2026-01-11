package com.peerislands.ecommerce.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OrderRequest(
        @NotNull
        @Valid
        List<OrderItemRequest> items
) {
    public record OrderItemRequest(
            @NotBlank
            String productId,

            @Min(1)
            @NotNull
            Integer quantity
    ) {}
}