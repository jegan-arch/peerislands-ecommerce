package com.peerislands.ecommerce.service.impl.validator;

import com.peerislands.ecommerce.exception.BusinessException;
import com.peerislands.ecommerce.exception.ErrorCode;
import com.peerislands.ecommerce.model.CreateOrderCommand;
import com.peerislands.ecommerce.model.Product;
import com.peerislands.ecommerce.service.InventoryService;
import com.peerislands.ecommerce.service.validator.InventoryValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryValidatorTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryValidator inventoryValidator;

    @Test
    void validate_HasStock_ShouldPass() {
        String productId = "PROD-1";
        int qty = 5;
        CreateOrderCommand.OrderItemCommand itemCmd = new CreateOrderCommand.OrderItemCommand(productId, qty);
        Product product = new Product(productId, "Test Widget", BigDecimal.TEN);

        when(inventoryService.hasStock(productId, qty)).thenReturn(true);

        assertDoesNotThrow(() -> inventoryValidator.validate(itemCmd, product));

        verify(inventoryService).hasStock(productId, qty);
    }

    @Test
    void validate_NoStock_ShouldThrowException() {
        String productId = "PROD-1";
        int qty = 100;
        CreateOrderCommand.OrderItemCommand itemCmd = new CreateOrderCommand.OrderItemCommand(productId, qty);
        Product product = new Product(productId, "Test Widget", BigDecimal.TEN);

        when(inventoryService.hasStock(productId, qty)).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> inventoryValidator.validate(itemCmd, product));

        assertEquals(ErrorCode.INSUFFICIENT_STOCK, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Insufficient stock for 'Test Widget'"));

        verify(inventoryService).hasStock(productId, qty);
    }
}