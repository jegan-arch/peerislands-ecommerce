package com.peerislands.ecommerce.service.impl;

import com.peerislands.ecommerce.exception.BusinessException;
import com.peerislands.ecommerce.exception.ErrorCode;
import com.peerislands.ecommerce.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryCatalogServiceImplTest {

    private InMemoryCatalogServiceImpl catalogService;

    @BeforeEach
    void setUp() {
        catalogService = new InMemoryCatalogServiceImpl();
        catalogService.init();
    }

    @Test
    void getProduct_Success() {
        Product result = catalogService.getProduct("PROD-1");

        assertNotNull(result);
        assertEquals("PROD-1", result.id());
        assertEquals("Wireless Mouse", result.name());
        assertEquals(new BigDecimal("25.00"), result.price());
    }

    @Test
    void getProduct_NotFound_ThrowsException() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> catalogService.getProduct("UNKNOWN-ID"));

        assertEquals(ErrorCode.PRODUCT_NOT_FOUND, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Product not found"));
    }

    @Test
    void init_PopulatesDefaultCatalog() {
        assertDoesNotThrow(() -> catalogService.getProduct("PROD-1"));
        assertDoesNotThrow(() -> catalogService.getProduct("PROD-2"));
        assertDoesNotThrow(() -> catalogService.getProduct("PROD-3"));
    }
}