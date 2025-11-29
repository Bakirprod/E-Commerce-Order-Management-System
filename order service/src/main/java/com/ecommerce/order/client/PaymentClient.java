package com.ecommerce.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class PaymentClient {
    
    private final WebClient webClient;
    
    @Value("${service.urls.payment}")
    private String paymentServiceUrl;
    
    public PaymentClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    public Mono<Boolean> processPayment(Long orderId, Long customerId, Double amount) {
        String paymentRequest = String.format(
            "{\"orderId\": %d, \"customerId\": %d, \"amount\": %.2f, \"paymentMethod\": \"CREDIT_CARD\"}",
            orderId, customerId, amount);
            
        return webClient.post()
                .uri(paymentServiceUrl + "/api/payments")
                .header("Content-Type", "application/json")
                .bodyValue(paymentRequest)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    System.out.println("Payment processed: " + response);
                    return true;
                })
                .onErrorReturn(false);
    }
}