package com.peerislands.ecommerce.service.validator;

import com.peerislands.ecommerce.model.CreateOrderCommand;
import com.peerislands.ecommerce.model.Product;

public interface OrderValidator {
    void validate(CreateOrderCommand.OrderItemCommand item, Product product);
}