package com.peerislands.ecommerce.service.impl;

import com.peerislands.ecommerce.entity.OrderEntity;
import com.peerislands.ecommerce.model.OrderStatus;
import com.peerislands.ecommerce.exception.BusinessException;
import com.peerislands.ecommerce.exception.ErrorCode;
import com.peerislands.ecommerce.model.CreateOrderCommand;
import com.peerislands.ecommerce.model.Order;
import com.peerislands.ecommerce.model.Product;
import com.peerislands.ecommerce.repository.OrderRepository;
import com.peerislands.ecommerce.service.CatalogService;
import com.peerislands.ecommerce.service.InventoryService;
import com.peerislands.ecommerce.service.validator.OrderValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CatalogService catalogService;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private OrderValidator inventoryValidator;

    private OrderServiceImpl orderService;
    private Product mockProduct;
    private OrderEntity mockOrderEntity;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        mockProduct = new Product("PROD-1", "Test Widget", new BigDecimal("100.00"));

        mockOrderEntity = OrderEntity.builder()
                .id(orderId)
                .customerId("user-1")
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .items(Collections.emptyList())
                .build();

        orderService = new OrderServiceImpl(orderRepository, catalogService, inventoryService, List.of(inventoryValidator));
    }

    @Test
    void createOrder_Success() {
        CreateOrderCommand cmd = new CreateOrderCommand(
                "user-1",
                List.of(new CreateOrderCommand.OrderItemCommand("PROD-1", 2))
        );

        when(catalogService.getProduct("PROD-1")).thenReturn(mockProduct);
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(mockOrderEntity);

        Order result = orderService.createOrder(cmd);

        assertNotNull(result);
        verify(inventoryService).reserveStock("PROD-1", 2);
    }

    @Test
    void createOrder_ProductNotFound_ThrowsBusinessException() {
        CreateOrderCommand cmd = new CreateOrderCommand(
                "user-1",
                List.of(new CreateOrderCommand.OrderItemCommand("INVALID", 1))
        );

        when(catalogService.getProduct("INVALID"))
                .thenThrow(new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.createOrder(cmd));
        assertEquals(ErrorCode.PRODUCT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void cancelOrder_AccessDenied_ThrowsBusinessException() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrderEntity));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.cancelOrder(orderId, "user-2")); // Wrong User

        assertEquals(ErrorCode.ACCESS_DENIED, ex.getErrorCode());
    }
}