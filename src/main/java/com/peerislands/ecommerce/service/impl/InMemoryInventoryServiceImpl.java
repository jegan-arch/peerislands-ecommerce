package com.peerislands.ecommerce.service.impl;

import com.peerislands.ecommerce.exception.BusinessException;
import com.peerislands.ecommerce.exception.ErrorCode;
import com.peerislands.ecommerce.service.InventoryService;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryInventoryServiceImpl implements InventoryService {

    private final Map<String, Integer> stockRegistry = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        stockRegistry.put("PROD-1", 100);
        stockRegistry.put("PROD-2", 50);
        stockRegistry.put("PROD-3", 2);
    }

    @Override
    public boolean hasStock(String productId, Integer quantity) {
        return stockRegistry.getOrDefault(productId, 0) >= quantity;
    }

    @Override
    public void reserveStock(String productId, Integer quantity) {
        stockRegistry.compute(productId, (id, currentStock) -> {
            if (currentStock == null) {
                throw new BusinessException(ErrorCode.INVENTORY_RECORD_NOT_FOUND, "No inventory record for: " + productId);
            }
            if (currentStock < quantity) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, "Insufficient stock for: " + productId);
            }
            return currentStock - quantity;
        });
    }

    @Override
    public void releaseStock(String productId, Integer quantity) {
        stockRegistry.computeIfPresent(productId, (id, currentStock) -> currentStock + quantity);
    }
}