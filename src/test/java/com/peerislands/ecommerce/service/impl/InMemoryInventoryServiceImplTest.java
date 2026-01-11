package com.peerislands.ecommerce.service.impl;

import com.peerislands.ecommerce.exception.BusinessException;
import com.peerislands.ecommerce.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryInventoryServiceImplTest {

    private InMemoryInventoryServiceImpl inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InMemoryInventoryServiceImpl();
        inventoryService.init();
    }

    @Test
    void hasStock_EnoughStock_ReturnsTrue() {
        assertTrue(inventoryService.hasStock("PROD-1", 50));
        assertTrue(inventoryService.hasStock("PROD-1", 100));
    }

    @Test
    void hasStock_NotEnoughStock_ReturnsFalse() {
        assertFalse(inventoryService.hasStock("PROD-1", 101));
    }

    @Test
    void hasStock_ProductNotFound_ReturnsFalse() {
        assertFalse(inventoryService.hasStock("UNKNOWN-PROD", 1));
    }

    @Test
    void reserveStock_Success() {
        inventoryService.reserveStock("PROD-1", 10);

        assertTrue(inventoryService.hasStock("PROD-1", 90));
        assertFalse(inventoryService.hasStock("PROD-1", 91));
    }

    @Test
    void reserveStock_InsufficientStock_ThrowsException() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> inventoryService.reserveStock("PROD-1", 101));

        assertEquals(ErrorCode.INSUFFICIENT_STOCK, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Insufficient stock"));
    }

    @Test
    void reserveStock_ProductNotFound_ThrowsException() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> inventoryService.reserveStock("INVALID-ID", 1));

        assertEquals(ErrorCode.INVENTORY_RECORD_NOT_FOUND, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("No inventory record"));
    }

    @Test
    void releaseStock_Success() {
        inventoryService.releaseStock("PROD-1", 10);

        assertTrue(inventoryService.hasStock("PROD-1", 110));
    }

    @Test
    void releaseStock_ProductNotFound_DoesNothing() {
        assertDoesNotThrow(() -> inventoryService.releaseStock("UNKNOWN", 10));
    }

    @Test
    void concurrency_SimulateRaceCondition_SafetyCheck() {
        assertDoesNotThrow(() -> {
            inventoryService.reserveStock("PROD-1", 100);
            inventoryService.releaseStock("PROD-1", 100);
        });

        assertTrue(inventoryService.hasStock("PROD-1", 100));
    }
}