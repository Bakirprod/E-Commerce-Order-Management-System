package com.ecommerce.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ShippingClient {
    
    private final WebClient webClient;
    
    @Value("${service.urls.shipping}")
    private String shippingServiceUrl;
    
    public ShippingClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    public Mono<Boolean> createShipping(Long orderId, Long customerId, Double totalAmount) {
        String shippingRequest = String.format(
            "{\"orderId\": %d, \"customerId\": %d, \"address\": \"Auto-generated address\", \"carrier\": \"DHL\", \"shippingCost\": %.2f}",
            orderId, customerId, totalAmount * 0.1); // 10% shipping cost
            
        return webClient.post()
                .uri(shippingServiceUrl + "/api/shippings")
                .header("Content-Type", "application/json")
                .bodyValue(shippingRequest)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    System.out.println("Shipping created: " + response);
                    return true;
                })
                .onErrorReturn(false);
    }
}