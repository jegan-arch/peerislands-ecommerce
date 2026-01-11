package com.peerislands.ecommerce.job;

import com.peerislands.ecommerce.entity.OrderEntity;
import com.peerislands.ecommerce.model.OrderStatus;
import com.peerislands.ecommerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderProcessingJob {

    private final OrderRepository orderRepository;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void processOrders() {
        log.info("Job: Checking for pending orders...");
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);
        List<OrderEntity> orders = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING, cutoff);

        for (OrderEntity order : orders) {
            log.info("Processing Order: {}", order.getId());
            order.setStatus(OrderStatus.PROCESSING);
        }
    }
}