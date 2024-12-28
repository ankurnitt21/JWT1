package com.example.NotificationService.service;

import com.example.NotificationService.entity.Notification;
import com.example.NotificationService.entity.Order;
import com.example.NotificationService.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public Optional<Notification> getNotificationById(String id) {
        return notificationRepository.findById(id);
    }

    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    @KafkaListener(topics = "notification_topic", groupId = "notification-group")
    public void listen(Map<String, Object> orderData) throws InterruptedException {
        System.out.println("Received order data: " + orderData);

        Notification notification = new Notification();
        try {
            // Convert the list of integers to LocalDateTime
            List<Integer> createdAtList = (List<Integer>) orderData.get("createdAt");
            LocalDateTime createdAt = LocalDateTime.of(
                    createdAtList.get(0), // year
                    createdAtList.get(1), // month
                    createdAtList.get(2), // day
                    createdAtList.get(3), // hour
                    createdAtList.get(4), // minute
                    createdAtList.get(5)  // second
            );
            notification.setCreatedAt(createdAt);
            notification.setMessage("Order placed for user " + orderData.get("userId"));
            notification.setUserId((String) orderData.get("userId"));
            notification.setRead(false);
            Thread.sleep(20000);   // we can remove this
            createNotification(notification);
            System.out.println("Received notification for order ID: " + orderData.get("id"));
        } catch (ClassCastException e) {
            System.err.println("ClassCastException: " + e.getMessage());
            e.printStackTrace();
        }

        // Process the notification (e.g., send an email or SMS)
    }

    public Optional<Notification> updateNotification(String id, Notification notificationDetails) {
        return notificationRepository.findById(id).map(notification -> {
            notification.setUserId(notificationDetails.getUserId());
            notification.setMessage(notificationDetails.getMessage());
            notification.setCreatedAt(notificationDetails.getCreatedAt());
            notification.setRead(notificationDetails.isRead());
            return notificationRepository.save(notification);
        });
    }

    public void deleteNotification(String id) {
        notificationRepository.deleteById(id);
    }
}