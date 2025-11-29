package com.ecommerce.notification.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long customerId;
    private Long orderId;
    
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    
    private String subject;
    private String message;
    private String recipientEmail;
    private String recipientPhone;
    
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;
    
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    
    // FIXED: Add ORDER_CONFIRMATION to the enum
    public enum NotificationType {
        EMAIL, SMS, PUSH, ORDER_CONFIRMATION, PAYMENT_CONFIRMATION, SHIPPING_UPDATE
    }
    
    public enum NotificationStatus {
        PENDING, SENT, FAILED
    }
    
    // Constructors
    public Notification() {}
    
    public Notification(Long customerId, Long orderId, NotificationType type, String subject, String message, String recipientEmail) {
        this.customerId = customerId;
        this.orderId = orderId;
        this.type = type;
        this.subject = subject;
        this.message = message;
        this.recipientEmail = recipientEmail;
        this.status = NotificationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
    
    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
    
    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = NotificationStatus.PENDING;
        }
    }
}