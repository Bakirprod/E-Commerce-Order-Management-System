package com.ecommerce.notification.service;

import com.ecommerce.notification.event.OrderCreatedEvent;
import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.repository.NotificationRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class OrderEventConsumer {

    private final NotificationRepository notificationRepository;

    public OrderEventConsumer(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.order-created}")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        System.out.println("📨 NOTIFICATION SERVICE: Received OrderCreatedEvent for order: " + event.getOrderNumber());
        
        // Create notification from event
        Notification notification = new Notification(
            event.getCustomerId(),
            event.getOrderId(),
            Notification.NotificationType.EMAIL,
            "Order Confirmed - " + event.getOrderNumber(),
            "Your order " + event.getOrderNumber() + " for $" + event.getTotalAmount() + " has been processed successfully!",
            "customer@example.com"
        );
        
        notificationRepository.save(notification);
        System.out.println("✅ NOTIFICATION SERVICE: Notification created from event for order: " + event.getOrderNumber());
    }
}