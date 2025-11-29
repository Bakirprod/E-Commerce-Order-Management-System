package com.ecommerce.notification.controller;

import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @PostMapping
    public ResponseEntity<Notification> createNotification(@RequestBody Notification notification) {
        Notification createdNotification = notificationService.createNotification(notification);
        return ResponseEntity.ok(createdNotification);
    }
    
    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long id) {
        return notificationService.getNotificationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Notification>> getNotificationsByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(notificationService.getNotificationsByCustomerId(customerId));
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Notification>> getNotificationsByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(notificationService.getNotificationsByOrderId(orderId));
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Notification>> getNotificationsByType(@PathVariable Notification.NotificationType type) {
        return ResponseEntity.ok(notificationService.getNotificationsByType(type));
    }
    
    @PutMapping("/{id}/sent")
    public ResponseEntity<Notification> markAsSent(@PathVariable Long id) {
        try {
            Notification updatedNotification = notificationService.markAsSent(id);
            return ResponseEntity.ok(updatedNotification);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/failed")
    public ResponseEntity<Notification> markAsFailed(@PathVariable Long id) {
        try {
            Notification updatedNotification = notificationService.markAsFailed(id);
            return ResponseEntity.ok(updatedNotification);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}