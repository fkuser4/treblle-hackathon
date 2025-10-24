package com.hackathon.demo.service;

import com.hackathon.demo.model.Order;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    private final List<Order> orders = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public List<Order> getAllOrders() {
        return new ArrayList<>(orders);
    }

    public Optional<Order> getOrderById(Long id) {
        return orders.stream().filter(o -> o.getId().equals(id)).findFirst();
    }

    public Order createOrder(Order order) {
        order.setId(idGenerator.getAndIncrement());
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("PENDING");
        orders.add(order);
        return order;
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orders.stream().filter(o -> o.getUserId().equals(userId)).toList();
    }
}