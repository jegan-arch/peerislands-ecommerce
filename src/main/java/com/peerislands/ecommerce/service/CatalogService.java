package com.peerislands.ecommerce.service;

import com.peerislands.ecommerce.model.Product;

public interface CatalogService {
    Product getProduct(String productId);
}