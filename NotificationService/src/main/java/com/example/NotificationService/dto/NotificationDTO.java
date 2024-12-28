package com.example.NotificationService.dto;

import java.time.LocalDateTime;

public class NotificationDTO {
    private String id;
    private String userId;
    private String message;
    private LocalDateTime createdAt;
    private boolean read;

    public NotificationDTO(String id, String userId, String message, LocalDateTime createdAt, boolean read) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.createdAt = createdAt;
        this.read = read;
    }

    public NotificationDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    // Getters and Setters
}