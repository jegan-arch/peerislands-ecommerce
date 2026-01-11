package com.peerislands.ecommerce.service.impl;

import com.peerislands.ecommerce.entity.OrderEntity;
import com.peerislands.ecommerce.entity.OrderItemEntity;
import com.peerislands.ecommerce.model.OrderStatus;
import com.peerislands.ecommerce.exception.BusinessException;
import com.peerislands.ecommerce.exception.ErrorCode;
import com.peerislands.ecommerce.model.CreateOrderCommand;
import com.peerislands.ecommerce.model.Order;
import com.peerislands.ecommerce.model.OrderItem;
import com.peerislands.ecommerce.model.Product;
import com.peerislands.ecommerce.repository.OrderRepository;
import com.peerislands.ecommerce.service.CatalogService;
import com.peerislands.ecommerce.service.InventoryService;
import com.peerislands.ecommerce.service.OrderService;
import com.peerislands.ecommerce.service.validator.OrderValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CatalogService catalogService;
    private final InventoryService inventoryService;
    private final List<OrderValidator> orderValidators;

    @Override
    @Transactional
    public Order createOrder(CreateOrderCommand command) {
        OrderEntity entity = OrderEntity.builder()
                .customerId(command.customerId())
                .status(OrderStatus.PENDING)
                .build();

        List<OrderItemEntity> items = command.items().stream()
                .map(cmd -> {
                    Product product = catalogService.getProduct(cmd.productId());

                    orderValidators.forEach(v -> v.validate(cmd, product));

                    inventoryService.reserveStock(product.id(), cmd.quantity());

                    return OrderItemEntity.builder()
                            .productId(product.id())
                            .quantity(cmd.quantity())
                            .price(product.price())
                            .build();
                })
                .collect(Collectors.toList());

        entity.setItems(items);

        try {
            OrderEntity savedEntity = orderRepository.save(entity);
            return mapToDomain(savedEntity);
        } catch (Exception e) {
            for (OrderItemEntity item : items) {
                inventoryService.releaseStock(item.getProductId(), item.getQuantity());
            }
            throw e;
        }
    }

    @Override
    public Order getOrder(UUID id, String customerId) {
        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + id));

        if (!entity.getCustomerId().equals(customerId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return mapToDomain(entity);
    }

    @Override
    public Page<Order> getAllOrders(String customerId, String statusFilter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderEntity> entities;

        if (statusFilter != null && !statusFilter.isBlank()) {
            try {
                OrderStatus status = OrderStatus.valueOf(statusFilter.toUpperCase());
                entities = orderRepository.findByCustomerIdAndStatus(customerId, status, pageable);
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "Invalid status: " + statusFilter);
            }
        } else {
            entities = orderRepository.findByCustomerId(customerId, pageable);
        }

        return entities.map(this::mapToDomain);
    }

    @Override
    @Transactional
    public Order cancelOrder(UUID id, String customerId) {
        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + id));

        if (!entity.getCustomerId().equals(customerId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if (entity.getCreatedAt().plusMinutes(5).isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.CANCELLATION_EXPIRED);
        }

        if (entity.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS, "Current status: " + entity.getStatus());
        }

        for (OrderItemEntity item : entity.getItems()) {
            inventoryService.releaseStock(item.getProductId(), item.getQuantity());
        }

        entity.setStatus(OrderStatus.CANCELLED);
        return mapToDomain(orderRepository.save(entity));
    }

    private Order mapToDomain(OrderEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
                .map(i -> new OrderItem(i.getProductId(), i.getQuantity(), i.getPrice()))
                .collect(Collectors.toList());
        return new Order(entity.getId(), entity.getCustomerId(), entity.getStatus(), items, entity.getCreatedAt());
    }
}