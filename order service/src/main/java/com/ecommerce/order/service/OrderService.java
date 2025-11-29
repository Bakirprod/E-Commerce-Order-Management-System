package com.ecommerce.order.service;

import com.ecommerce.order.event.OrderCreatedEvent;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final WebClient webClient;
    private final OrderEventPublisher orderEventPublisher;

    // Updated constructor with OrderEventPublisher
    public OrderService(OrderRepository orderRepository, 
                       WebClient.Builder webClientBuilder,
                       OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.webClient = webClientBuilder.build();
        this.orderEventPublisher = orderEventPublisher;
    }
    
    public Order createOrder(Order order) {
        try {
            System.out.println("🚀 ========== HYBRID SYNC/ASYNC COMMUNICATION TEST ==========");
            System.out.println("📦 Customer: " + order.getCustomerId() + ", Amount: " + order.getTotalAmount());
            System.out.println("📦 Items: " + order.getItems().size() + " products");
            
            // Save order first
            order.setStatus(Order.OrderStatus.PENDING);
            Order savedOrder = orderRepository.save(order);
            System.out.println("✅ ORDER SAVED - ID: " + savedOrder.getId() + ", Number: " + savedOrder.getOrderNumber());
            
            // STEP 1: Customer Validation
            System.out.println("1. 🔍 CALLING CUSTOMER SERVICE: http://customer-service:8083/api/customers/" + savedOrder.getCustomerId());
            try {
                String customerResponse = webClient.get()
                        .uri("http://customer-service:8083/api/customers/{customerId}", savedOrder.getCustomerId())
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                
                System.out.println("📞 RAW CUSTOMER RESPONSE: " + customerResponse);
                
                // FIXED: Better customer validation logic
                boolean isCustomerValid = customerResponse != null && 
                                         !customerResponse.contains("404") && 
                                         !customerResponse.contains("Not Found") &&
                                         customerResponse.contains("\"id\"");
                
                System.out.println("✅ CUSTOMER VALIDATION RESULT: " + isCustomerValid);
                
                if (!isCustomerValid) {
                    throw new RuntimeException("Customer not found or invalid response");
                }
            } catch (Exception e) {
                System.out.println("❌ CUSTOMER SERVICE ERROR: " + e.getMessage());
                savedOrder.setStatus(Order.OrderStatus.CANCELLED);
                orderRepository.save(savedOrder);
                throw new RuntimeException("Customer validation failed: " + e.getMessage());
            }
            
            // STEP 2: Inventory Check
            System.out.println("2. 📦 CALLING INVENTORY SERVICE (Stock Check)...");
            for (var item : savedOrder.getItems()) {
                try {
                    System.out.println("   🔍 Checking product " + item.getProductId() + " at: http://inventory-service:8082/api/products/" + item.getProductId());
                    String inventoryResponse = webClient.get()
                            .uri("http://inventory-service:8082/api/products/{productId}", item.getProductId())
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();
                    System.out.println("   ✅ Product " + item.getProductId() + " stock check: " + (inventoryResponse != null ? "AVAILABLE" : "NOT AVAILABLE"));
                    if (inventoryResponse == null || inventoryResponse.contains("404")) {
                        throw new RuntimeException("Product " + item.getProductId() + " not found");
                    }
                } catch (Exception e) {
                    System.out.println("   ❌ Product " + item.getProductId() + " stock check failed: " + e.getMessage());
                    savedOrder.setStatus(Order.OrderStatus.CANCELLED);
                    orderRepository.save(savedOrder);
                    throw new RuntimeException("Inventory check failed: " + e.getMessage());
                }
            }
            
            // STEP 3: Payment Processing
            System.out.println("3. 💳 CALLING PAYMENT SERVICE: http://payment-service:8084/api/payments");
            try {
                String paymentRequest = String.format(
                    "{\"orderId\": %d, \"customerId\": %d, \"amount\": %.2f, \"paymentMethod\": \"CREDIT_CARD\", \"status\": \"PENDING\"}",
                    savedOrder.getId(), savedOrder.getCustomerId(), savedOrder.getTotalAmount());
                    
                System.out.println("   💰 Payment Request: " + paymentRequest);
                
                String paymentResponse = webClient.post()
                        .uri("http://payment-service:8084/api/payments")
                        .header("Content-Type", "application/json")
                        .bodyValue(paymentRequest)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                System.out.println("✅ PAYMENT SERVICE RESPONSE: " + (paymentResponse != null ? "SUCCESS - Payment Processed" : "FAILED - No Response"));
                if (paymentResponse == null) {
                    throw new RuntimeException("Payment processing failed");
                }
            } catch (Exception e) {
                System.out.println("❌ PAYMENT SERVICE ERROR: " + e.getMessage());
                savedOrder.setStatus(Order.OrderStatus.CANCELLED);
                orderRepository.save(savedOrder);
                throw new RuntimeException("Payment processing failed: " + e.getMessage());
            }
            
            // STEP 4: Shipping Creation
            System.out.println("4. 🚚 CALLING SHIPPING SERVICE: http://shipping-service:8085/api/shippings");
            try {
                String shippingRequest = String.format(
                    "{\"orderId\": %d, \"customerId\": %d, \"address\": \"123 Main St, New York, NY 10001\", \"carrier\": \"DHL\", \"status\": \"PENDING\"}",
                    savedOrder.getId(), savedOrder.getCustomerId());
                    
                System.out.println("   📦 Shipping Request: " + shippingRequest);
                
                String shippingResponse = webClient.post()
                        .uri("http://shipping-service:8085/api/shippings")
                        .header("Content-Type", "application/json")
                        .bodyValue(shippingRequest)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                System.out.println("✅ SHIPPING SERVICE RESPONSE: " + (shippingResponse != null ? "SUCCESS - Shipping Created" : "FAILED - No Response"));
            } catch (Exception e) {
                System.out.println("⚠️  SHIPPING SERVICE ERROR (non-critical): " + e.getMessage());
                // Continue anyway - shipping is not critical
            }
            
            // STEP 5: Notification
            System.out.println("5. 📧 CALLING NOTIFICATION SERVICE: http://notification-service:8086/api/notifications");
            try {
                String notificationRequest = String.format(
                    "{\"customerId\": %d, \"orderId\": %d, \"type\": \"EMAIL\", \"subject\": \"Order Confirmation - %s\", \"message\": \"Your order %s for $%.2f has been confirmed! Thank you for your purchase.\", \"recipientEmail\": \"customer@example.com\", \"status\": \"PENDING\"}",
                    savedOrder.getCustomerId(), savedOrder.getId(), savedOrder.getOrderNumber(), savedOrder.getOrderNumber(), savedOrder.getTotalAmount());
                    
                System.out.println("   📧 Notification Request: " + notificationRequest);
                
                String notificationResponse = webClient.post()
                        .uri("http://notification-service:8086/api/notifications")
                        .header("Content-Type", "application/json")
                        .bodyValue(notificationRequest)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                System.out.println("✅ NOTIFICATION SERVICE RESPONSE: " + (notificationResponse != null ? "SUCCESS - Notification Sent" : "FAILED - No Response"));
            } catch (Exception e) {
                System.out.println("❌ NOTIFICATION SERVICE ERROR: " + e.getMessage());
                // Continue anyway - notification is not critical
            }
            
            // Finalize order
            savedOrder.setStatus(Order.OrderStatus.CONFIRMED);
            savedOrder.setUpdatedAt(LocalDateTime.now());
            Order confirmedOrder = orderRepository.save(savedOrder);
            
            // STEP 6: ASYNCHRONOUS EVENT PUBLISHING (NEW!)
            System.out.println("6. 📢 PUBLISHING ASYNCHRONOUS EVENTS TO RABBITMQ...");
            try {
                // Create and publish OrderCreatedEvent
                OrderCreatedEvent orderEvent = new OrderCreatedEvent(
                    confirmedOrder.getId(),
                    confirmedOrder.getOrderNumber(),
                    confirmedOrder.getCustomerId(),
                    BigDecimal.valueOf(confirmedOrder.getTotalAmount()),
                    confirmedOrder.getCreatedAt(),
                    confirmedOrder.getItems().stream()
                        .map(item -> new OrderCreatedEvent.OrderItem(
                            item.getProductId(),
                            item.getProductName(),
                            item.getQuantity(),
                            BigDecimal.valueOf(item.getPrice())
                        ))
                        .toList()
                );
                
                orderEventPublisher.publishOrderCreatedEvent(orderEvent);
                System.out.println("✅ ASYNC EVENT: OrderCreatedEvent published to RabbitMQ!");
                
            } catch (Exception e) {
                System.out.println("⚠️  ASYNC EVENT ERROR: " + e.getMessage());
                // Continue anyway - async events shouldn't block the main flow
            }
            
            System.out.println("🎉 ========== HYBRID SYNC/ASYNC TEST COMPLETED ==========");
            System.out.println("📋 Order " + savedOrder.getOrderNumber() + " - ALL SERVICES CONTACTED!");
            System.out.println("🏆 SYNCHRONOUS + ASYNCHRONOUS COMMUNICATION DEMONSTRATION COMPLETE!");
            return confirmedOrder;
            
        } catch (Exception e) {
            System.out.println("💥 ========== ORDER PROCESSING FAILED ==========");
            System.out.println("❌ Error: " + e.getMessage());
            
            // Update order status to cancelled if it exists
            if (order.getId() != null) {
                try {
                    order.setStatus(Order.OrderStatus.CANCELLED);
                    orderRepository.save(order);
                    System.out.println("📝 Order marked as CANCELLED due to failure");
                } catch (Exception ex) {
                    System.out.println("⚠️  Could not update order status: " + ex.getMessage());
                }
            }
            
            throw new RuntimeException("Order processing failed: " + e.getMessage());
        }
    }
    
    // GET ALL ORDERS
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    // GET ORDER BY ID
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }
    
    // GET ORDER BY ORDER NUMBER
    public Optional<Order> getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }
    
    // GET ORDERS BY CUSTOMER ID
    public List<Order> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }
    
    // UPDATE ORDER STATUS
    public Order updateOrderStatus(Long id, Order.OrderStatus status) {
        Optional<Order> optionalOrder = orderRepository.findById(id);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setStatus(status);
            order.setUpdatedAt(LocalDateTime.now());
            return orderRepository.save(order);
        }
        throw new RuntimeException("Order not found with id: " + id);
    }
    
    // DELETE ORDER
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}