package com.peerislands.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.peerislands.ecommerce.dto.OrderRequest;
import com.peerislands.ecommerce.model.OrderStatus;
import com.peerislands.ecommerce.exception.BusinessException;
import com.peerislands.ecommerce.exception.ErrorCode;
import com.peerislands.ecommerce.model.CreateOrderCommand;
import com.peerislands.ecommerce.model.Order;
import com.peerislands.ecommerce.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    private final String CUSTOMER_ID = "user-1";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createOrder_ValidRequest_Returns200() throws Exception {
        OrderRequest request = new OrderRequest(
                List.of(new OrderRequest.OrderItemRequest("PROD-1", 1))
        );

        Order mockOrder = new Order(UUID.randomUUID(), CUSTOMER_ID, OrderStatus.PENDING, Collections.emptyList(), LocalDateTime.now());
        when(orderService.createOrder(any(CreateOrderCommand.class))).thenReturn(mockOrder);

        mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-Id", CUSTOMER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(CUSTOMER_ID))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createOrder_InvalidInput_Returns400() throws Exception {
        OrderRequest request = new OrderRequest(
                List.of(new OrderRequest.OrderItemRequest("PROD-1", 0))
        );

        mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-Id", CUSTOMER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERR_002"));
    }

    @Test
    void getOrder_NotFound_Returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.getOrder(id, CUSTOMER_ID))
                .thenThrow(new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        mockMvc.perform(get("/api/v1/orders/" + id)
                        .header("X-User-Id", CUSTOMER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORD_001"));
    }

    @Test
    void getAllOrders_ReturnsPagedResponse() throws Exception {
        Page<Order> emptyPage = new PageImpl<>(Collections.emptyList());
        when(orderService.getAllOrders(anyString(), any(), anyInt(), anyInt()))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/orders")
                        .header("X-User-Id", CUSTOMER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.page").value(0));
    }
}