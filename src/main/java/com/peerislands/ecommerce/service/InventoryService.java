package com.peerislands.ecommerce.service;

public interface InventoryService {
    boolean hasStock(String productId, Integer quantity);
    void reserveStock(String productId, Integer quantity);
    void releaseStock(String productId, Integer quantity);
}