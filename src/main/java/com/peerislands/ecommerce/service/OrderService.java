package com.peerislands.ecommerce.service;

import com.peerislands.ecommerce.model.CreateOrderCommand;
import com.peerislands.ecommerce.model.Order;
import org.springframework.data.domain.Page;
import java.util.UUID;

public interface OrderService {
    Order createOrder(CreateOrderCommand command);
    Order getOrder(UUID id, String customerId);
    Page<Order> getAllOrders(String customerId, String statusFilter, int page, int size);
    Order cancelOrder(UUID id, String customerId);
}