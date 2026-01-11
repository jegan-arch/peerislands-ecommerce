package com.peerislands.ecommerce.service;

import com.peerislands.ecommerce.model.Product;

public interface ProductService {
    Product getProduct(String productId);

    void releaseStock(String productId, Integer quantity);
}