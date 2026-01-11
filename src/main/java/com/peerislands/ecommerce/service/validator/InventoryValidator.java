package com.peerislands.ecommerce.service.validator;

import com.peerislands.ecommerce.exception.BusinessException;
import com.peerislands.ecommerce.exception.ErrorCode;
import com.peerislands.ecommerce.model.CreateOrderCommand;
import com.peerislands.ecommerce.model.Product;
import com.peerislands.ecommerce.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@RequiredArgsConstructor
public class InventoryValidator implements OrderValidator {

    private final InventoryService inventoryService;

    @Override
    public void validate(CreateOrderCommand.OrderItemCommand item, Product product) {
        if (!inventoryService.hasStock(item.productId(), item.quantity())) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK,
                    String.format("Insufficient stock for '%s'.", product.name()));
        }
    }
}