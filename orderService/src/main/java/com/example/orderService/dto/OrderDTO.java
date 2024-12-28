package com.example.orderService.dto;

import java.time.LocalDateTime;
import java.util.List;

public class OrderDTO {
    private String id;
    private String userId;
    private List<String> productIds;
    private double totalAmount;
    private String status;
    private LocalDateTime createdAt;

    // Getters and Setters
}