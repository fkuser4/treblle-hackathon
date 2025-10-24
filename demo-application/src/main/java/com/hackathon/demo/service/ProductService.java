package com.hackathon.demo.service;

import com.hackathon.demo.model.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductService {

    private final List<Product> products = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public ProductService() {
        // Initialize with sample data
        products.add(Product.builder().id(idGenerator.getAndIncrement()).name("Laptop").description("High-performance laptop").price(new BigDecimal("999.99")).stock(50).build());
        products.add(Product.builder().id(idGenerator.getAndIncrement()).name("Mouse").description("Wireless mouse").price(new BigDecimal("29.99")).stock(200).build());
        products.add(Product.builder().id(idGenerator.getAndIncrement()).name("Keyboard").description("Mechanical keyboard").price(new BigDecimal("89.99")).stock(150).build());
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(products);
    }

    public Optional<Product> getProductById(Long id) {
        return products.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public Product createProduct(Product product) {
        product.setId(idGenerator.getAndIncrement());
        products.add(product);
        return product;
    }

    public Optional<Product> updateProduct(Long id, Product updatedProduct) {
        return getProductById(id).map(product -> {
            product.setName(updatedProduct.getName());
            product.setDescription(updatedProduct.getDescription());
            product.setPrice(updatedProduct.getPrice());
            product.setStock(updatedProduct.getStock());
            return product;
        });
    }

    public boolean deleteProduct(Long id) {
        return products.removeIf(p -> p.getId().equals(id));
    }
}