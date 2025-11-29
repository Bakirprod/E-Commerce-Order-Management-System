package com.ecommerce.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class InventoryClient {
    
    private final WebClient webClient;
    
    @Value("${service.urls.inventory}")
    private String inventoryServiceUrl;
    
    public InventoryClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    public Mono<Boolean> checkStock(Long productId, Integer quantity) {
        return webClient.get()
                .uri(inventoryServiceUrl + "/api/products/{productId}", productId)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    // Simple stock check - in real app, parse JSON response
                    System.out.println("Stock check for product " + productId + ": " + response);
                    return response != null && !response.contains("404") && !response.contains("null");
                })
                .onErrorReturn(false);
    }
    
    public Mono<Boolean> updateStock(Long productId, Integer quantity) {
        return webClient.patch()
                .uri(inventoryServiceUrl + "/api/products/{productId}/stock?quantity={quantity}", 
                     productId, quantity)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    System.out.println("Stock updated for product " + productId + ": " + response);
                    return true;
                })
                .onErrorReturn(false);
    }
}