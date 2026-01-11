package com.peerislands.ecommerce.service.impl;

import com.peerislands.ecommerce.entity.OrderEntity;
import com.peerislands.ecommerce.entity.OrderItemEntity;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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

    @Mock private OrderRepository orderRepository;
    @Mock private CatalogService catalogService;
    @Mock private InventoryService inventoryService;
    @Mock private OrderValidator mockValidator;

    private OrderServiceImpl orderService;

    private Product mockProduct;
    private OrderEntity mockOrderEntity;
    private UUID orderId;
    private final String CUSTOMER_ID = "user-1";

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        mockProduct = new Product("PROD-1", "Test Widget", new BigDecimal("100.00"));

        mockOrderEntity = OrderEntity.builder()
                .id(orderId)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .items(Collections.emptyList())
                .build();

        orderService = new OrderServiceImpl(orderRepository, catalogService, inventoryService, List.of(mockValidator));
    }

    @Test
    void createOrder_Success() {
        CreateOrderCommand cmd = new CreateOrderCommand(
                CUSTOMER_ID,
                List.of(new CreateOrderCommand.OrderItemCommand("PROD-1", 2))
        );

        when(catalogService.getProduct("PROD-1")).thenReturn(mockProduct);
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(mockOrderEntity);

        Order result = orderService.createOrder(cmd);

        assertNotNull(result);
        assertEquals(CUSTOMER_ID, result.customerId());

        verify(catalogService).getProduct("PROD-1");
        verify(mockValidator).validate(any(), eq(mockProduct));
        verify(inventoryService).reserveStock("PROD-1", 2);
        verify(orderRepository).save(any(OrderEntity.class));
    }

    @Test
    void createOrder_ValidationFails_ShouldNotReserveOrSave() {
        CreateOrderCommand cmd = new CreateOrderCommand(
                CUSTOMER_ID,
                List.of(new CreateOrderCommand.OrderItemCommand("PROD-1", 500))
        );

        when(catalogService.getProduct("PROD-1")).thenReturn(mockProduct);
        doThrow(new BusinessException(ErrorCode.INSUFFICIENT_STOCK))
                .when(mockValidator).validate(any(), any());

        assertThrows(BusinessException.class, () -> orderService.createOrder(cmd));

        verify(inventoryService, never()).reserveStock(any(), anyInt());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_InventoryReserveFails_ShouldNotSave() {
        CreateOrderCommand cmd = new CreateOrderCommand(
                CUSTOMER_ID,
                List.of(new CreateOrderCommand.OrderItemCommand("PROD-1", 2))
        );

        when(catalogService.getProduct("PROD-1")).thenReturn(mockProduct);
        doThrow(new BusinessException(ErrorCode.INSUFFICIENT_STOCK))
                .when(inventoryService).reserveStock(any(), anyInt());

        assertThrows(BusinessException.class, () -> orderService.createOrder(cmd));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_DbSaveFails_ShouldCompensateStock() {
        CreateOrderCommand cmd = new CreateOrderCommand(
                CUSTOMER_ID,
                List.of(new CreateOrderCommand.OrderItemCommand("PROD-1", 2))
        );

        when(catalogService.getProduct("PROD-1")).thenReturn(mockProduct);
        doNothing().when(inventoryService).reserveStock("PROD-1", 2);
        when(orderRepository.save(any())).thenThrow(new RuntimeException("DB Connection Died"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.createOrder(cmd));
        assertEquals("DB Connection Died", ex.getMessage());

        verify(inventoryService).releaseStock("PROD-1", 2);
    }

    @Test
    void getOrder_Success() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrderEntity));

        Order result = orderService.getOrder(orderId, CUSTOMER_ID);
        assertEquals(orderId, result.id());
    }

    @Test
    void getOrder_NotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.getOrder(orderId, CUSTOMER_ID));
        assertEquals(ErrorCode.ORDER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getOrder_AccessDenied() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrderEntity));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.getOrder(orderId, "hacker-user"));
        assertEquals(ErrorCode.ACCESS_DENIED, ex.getErrorCode());
    }

    @Test
    void getAllOrders_NoFilter() {
        Page<OrderEntity> page = new PageImpl<>(List.of(mockOrderEntity));
        when(orderRepository.findByCustomerId(eq(CUSTOMER_ID), any(Pageable.class)))
                .thenReturn(page);

        Page<Order> result = orderService.getAllOrders(CUSTOMER_ID, null, 0, 10);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllOrders_WithValidFilter() {
        Page<OrderEntity> page = new PageImpl<>(List.of(mockOrderEntity));
        when(orderRepository.findByCustomerIdAndStatus(eq(CUSTOMER_ID), eq(OrderStatus.PENDING), any(Pageable.class)))
                .thenReturn(page);

        Page<Order> result = orderService.getAllOrders(CUSTOMER_ID, "PENDING", 0, 10);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllOrders_WithInvalidFilter() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.getAllOrders(CUSTOMER_ID, "JUNK_STATUS", 0, 10));

        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
    }

    @Test
    void cancelOrder_Success() {
        OrderItemEntity item = OrderItemEntity.builder()
                .productId("PROD-1").quantity(5).build();
        mockOrderEntity.setItems(List.of(item));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrderEntity));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.cancelOrder(orderId, CUSTOMER_ID);

        assertEquals(OrderStatus.CANCELLED, result.status());
        verify(inventoryService).releaseStock("PROD-1", 5);
    }

    @Test
    void cancelOrder_TimeExpired() {
        mockOrderEntity.setCreatedAt(LocalDateTime.now().minusMinutes(10));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrderEntity));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.cancelOrder(orderId, CUSTOMER_ID));

        assertEquals(ErrorCode.CANCELLATION_EXPIRED, ex.getErrorCode());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelOrder_WrongStatus() {
        mockOrderEntity.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrderEntity));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.cancelOrder(orderId, CUSTOMER_ID));

        assertEquals(ErrorCode.INVALID_ORDER_STATUS, ex.getErrorCode());
    }
}