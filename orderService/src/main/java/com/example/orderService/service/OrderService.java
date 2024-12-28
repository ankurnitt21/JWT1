package com.example.orderService.service;

import com.example.orderService.entity.Order;
import com.example.orderService.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderService {
    private static final String TOPIC = "notification_topic";
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(String id) {
        return orderRepository.findById(id);
    }

    public Order createOrder(Order order) {
        Order created_order = orderRepository.save(order);
        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("id", created_order.getId());
        orderMap.put("userId", created_order.getUserId());
        orderMap.put("createdAt", created_order.getCreatedAt());
        kafkaTemplate.send(TOPIC, orderMap);
        System.out.println("Notification sent for order ID: " + order.getUserId());
        return created_order;
    }

    public Optional<Order> updateOrder(String id, Order orderDetails) {
        return orderRepository.findById(id).map(order -> {
            order.setUserId(orderDetails.getUserId());
            order.setProductIds(orderDetails.getProductIds());
            order.setTotalAmount(orderDetails.getTotalAmount());
            order.setStatus(orderDetails.getStatus());
            order.setCreatedAt(orderDetails.getCreatedAt());
            return orderRepository.save(order);
        });
    }

    public void deleteOrder(String id) {
        orderRepository.deleteById(id);
    }
}