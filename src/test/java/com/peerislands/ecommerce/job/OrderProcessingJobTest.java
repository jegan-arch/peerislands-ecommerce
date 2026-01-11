package com.peerislands.ecommerce.job;

import com.peerislands.ecommerce.entity.OrderEntity;
import com.peerislands.ecommerce.model.OrderStatus;
import com.peerislands.ecommerce.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderProcessingJobTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderProcessingJob orderProcessingJob;

    @Test
    void processOrders_FoundPendingOrders_UpdatesStatusToProcessing() {
        OrderEntity order1 = OrderEntity.builder()
                .id(UUID.randomUUID())
                .status(OrderStatus.PENDING)
                .build();

        OrderEntity order2 = OrderEntity.builder()
                .id(UUID.randomUUID())
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findByStatusAndCreatedAtBefore(eq(OrderStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(order1, order2));

        orderProcessingJob.processOrders();

        assertEquals(OrderStatus.PROCESSING, order1.getStatus(), "Order 1 status should be updated");
        assertEquals(OrderStatus.PROCESSING, order2.getStatus(), "Order 2 status should be updated");
    }

    @Test
    void processOrders_NoOrdersFound_DoesNothing() {
        when(orderRepository.findByStatusAndCreatedAtBefore(any(), any()))
                .thenReturn(Collections.emptyList());

        orderProcessingJob.processOrders();

        verify(orderRepository).findByStatusAndCreatedAtBefore(eq(OrderStatus.PENDING), any(LocalDateTime.class));
    }

    @Test
    void processOrders_VerifiesTimeCutoffLogic() {

        when(orderRepository.findByStatusAndCreatedAtBefore(any(), any()))
                .thenReturn(Collections.emptyList());

        orderProcessingJob.processOrders();

        ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(orderRepository).findByStatusAndCreatedAtBefore(eq(OrderStatus.PENDING), timeCaptor.capture());

        LocalDateTime capturedTime = timeCaptor.getValue();
        LocalDateTime now = LocalDateTime.now();

        assertTrue(capturedTime.isBefore(now.minusMinutes(4).minusSeconds(59)));
        assertTrue(capturedTime.isAfter(now.minusMinutes(5).minusSeconds(5)));
    }
}