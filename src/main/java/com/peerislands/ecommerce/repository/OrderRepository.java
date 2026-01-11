package com.peerislands.ecommerce.repository;

import com.peerislands.ecommerce.entity.OrderEntity;
import com.peerislands.ecommerce.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    List<OrderEntity> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime timestamp);
    Page<OrderEntity> findByCustomerId(String customerId, Pageable pageable);
    Page<OrderEntity> findByCustomerIdAndStatus(String customerId, OrderStatus status, Pageable pageable);
}