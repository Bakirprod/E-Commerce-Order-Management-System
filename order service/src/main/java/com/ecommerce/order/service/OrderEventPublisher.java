package com.ecommerce.order.service;

import com.ecommerce.order.event.OrderCreatedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange.order-events}")
    private String orderEventsExchange;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        System.out.println("📢 PUBLISHING ORDER CREATED EVENT: " + event.getOrderNumber());
        rabbitTemplate.convertAndSend(orderEventsExchange, "order.created", event);
        System.out.println("✅ ORDER CREATED EVENT PUBLISHED SUCCESSFULLY!");
    }
}