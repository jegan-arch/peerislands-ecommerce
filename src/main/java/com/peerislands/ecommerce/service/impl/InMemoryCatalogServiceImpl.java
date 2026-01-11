package com.peerislands.ecommerce.service.impl;

import com.peerislands.ecommerce.exception.BusinessException;
import com.peerislands.ecommerce.exception.ErrorCode;
import com.peerislands.ecommerce.model.Product;
import com.peerislands.ecommerce.service.CatalogService;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryCatalogServiceImpl implements CatalogService {

    private final Map<String, Product> productCatalog = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        productCatalog.put("PROD-1", new Product("PROD-1", "Wireless Mouse", new BigDecimal("25.00")));
        productCatalog.put("PROD-2", new Product("PROD-2", "Mechanical Keyboard", new BigDecimal("150.00")));
        productCatalog.put("PROD-3", new Product("PROD-3", "iPhone 15", new BigDecimal("999.00")));
    }

    @Override
    public Product getProduct(String productId) {
        Product product = productCatalog.get(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + productId);
        }
        return product;
    }
}