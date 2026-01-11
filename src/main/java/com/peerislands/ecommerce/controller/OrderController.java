package com.peerislands.ecommerce.controller;

import com.peerislands.ecommerce.dto.OrderItemDto;
import com.peerislands.ecommerce.dto.OrderRequest;
import com.peerislands.ecommerce.dto.OrderResponse;
import com.peerislands.ecommerce.dto.PagedResponse;
import com.peerislands.ecommerce.model.CreateOrderCommand;
import com.peerislands.ecommerce.model.Order;
import com.peerislands.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader("X-User-Id") String customerId,
            @RequestBody @Valid OrderRequest request) {

        CreateOrderCommand command = new CreateOrderCommand(
                customerId,
                request.items().stream()
                        .map(i -> new CreateOrderCommand.OrderItemCommand(i.productId(), i.quantity()))
                        .collect(Collectors.toList())
        );

        return ResponseEntity.ok(mapToResponse(orderService.createOrder(command)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @RequestHeader("X-User-Id") String customerId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(mapToResponse(orderService.getOrder(id, customerId)));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<OrderResponse>> getAllOrders(
            @RequestHeader("X-User-Id") String customerId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Order> orderPage = orderService.getAllOrders(customerId, status, page, size);
        Page<OrderResponse> responsePage = orderPage.map(this::mapToResponse);
        return ResponseEntity.ok(new PagedResponse<>(responsePage));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @RequestHeader("X-User-Id") String customerId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(mapToResponse(orderService.cancelOrder(id, customerId)));
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemDto> itemDtos = order.items().stream()
                .map(i -> new OrderItemDto(i.productId(), i.quantity()))
                .collect(Collectors.toList());
        return new OrderResponse(order.id(), order.customerId(), order.status().name(), itemDtos, order.createdAt());
    }
}