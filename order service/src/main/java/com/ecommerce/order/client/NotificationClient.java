package com.ecommerce.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class NotificationClient {
    
    private final WebClient webClient;
    
    @Value("${service.urls.notification}")
    private String notificationServiceUrl;
    
    public NotificationClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    public Mono<Boolean> sendOrderConfirmation(Long orderId, Long customerId, String orderNumber) {
        String notificationRequest = String.format(
            "{\"customerId\": %d, \"orderId\": %d, \"type\": \"EMAIL\", \"subject\": \"Order Confirmation - %s\", \"message\": \"Your order has been confirmed! Order Number: %s\", \"recipientEmail\": \"customer@example.com\"}",
            customerId, orderId, orderNumber, orderNumber);
            
        return webClient.post()
                .uri(notificationServiceUrl + "/api/notifications")
                .header("Content-Type", "application/json")
                .bodyValue(notificationRequest)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    System.out.println("Notification sent: " + response);
                    return true;
                })
                .onErrorReturn(false);
    }
}