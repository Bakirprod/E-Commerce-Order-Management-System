package com.ecommerce.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class CustomerClient {
    
    private final WebClient webClient;
    
    @Value("${service.urls.customer}")
    private String customerServiceUrl;
    
    public CustomerClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    public Mono<Boolean> validateCustomer(Long customerId) {
        return webClient.get()
                .uri(customerServiceUrl + "/api/customers/{customerId}", customerId)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    System.out.println("📞 Customer validation response: " + response);
                    // Check if response contains valid customer data
                    boolean isValid = response != null && 
                                     !response.contains("404") && 
                                     !response.contains("null") &&
                                     response.contains("\"id\"") &&
                                     response.contains(customerId.toString());
                    System.out.println("✅ Customer validation result: " + isValid);
                    return isValid;
                })
                .onErrorResume(error -> {
                    System.out.println("❌ Customer validation error: " + error.getMessage());
                    return Mono.just(false);
                });
    }
    
    
    public Mono<String> validateCustomerTest(Long customerId) {
        return webClient.get()
                .uri(customerServiceUrl + "/api/customers/{customerId}", customerId)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(error -> {
                    System.out.println("❌ Customer validation error: " + error.getMessage());
                    return Mono.just("ERROR: " + error.getMessage());
                });
    }
    
    
    
}