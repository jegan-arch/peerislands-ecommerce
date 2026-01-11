package com.peerislands.ecommerce.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // General
    INTERNAL_ERROR("ERR_001", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST("ERR_002", "Invalid request parameters", HttpStatus.BAD_REQUEST),

    // Product / Catalog
    PRODUCT_NOT_FOUND("PROD_001", "Product not found in catalog", HttpStatus.NOT_FOUND),

    // Inventory
    INSUFFICIENT_STOCK("INV_001", "Insufficient stock for product", HttpStatus.UNPROCESSABLE_CONTENT),
    INVENTORY_RECORD_NOT_FOUND("INV_002", "Inventory record not found", HttpStatus.INTERNAL_SERVER_ERROR),

    // Order
    ORDER_NOT_FOUND("ORD_001", "Order not found", HttpStatus.NOT_FOUND),
    ACCESS_DENIED("ORD_002", "You do not have permission to access this order", HttpStatus.FORBIDDEN),
    CANCELLATION_EXPIRED("ORD_003", "Cancellation window of 5 minutes has expired", HttpStatus.UNPROCESSABLE_CONTENT),
    INVALID_ORDER_STATUS("ORD_004", "Order cannot be cancelled in its current status", HttpStatus.UNPROCESSABLE_CONTENT);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}