package com.ecommerce.shipping.controller;

import com.ecommerce.shipping.model.Shipping;
import com.ecommerce.shipping.service.ShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/shippings")
public class ShippingController {
    
    @Autowired
    private ShippingService shippingService;
    
    @PostMapping
    public ResponseEntity<Shipping> createShipping(@RequestBody Shipping shipping) {
        Shipping createdShipping = shippingService.createShipping(shipping);
        return ResponseEntity.ok(createdShipping);
    }
    
    @GetMapping
    public ResponseEntity<List<Shipping>> getAllShippings() {
        return ResponseEntity.ok(shippingService.getAllShippings());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Shipping> getShippingById(@PathVariable Long id) {
        return shippingService.getShippingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Shipping>> getShippingsByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(shippingService.getShippingsByOrderId(orderId));
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Shipping>> getShippingsByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(shippingService.getShippingsByCustomerId(customerId));
    }
    
    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<Shipping> getShippingByTrackingNumber(@PathVariable String trackingNumber) {
        return shippingService.getShippingByTrackingNumber(trackingNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<Shipping> updateShippingStatus(
            @PathVariable Long id, 
            @RequestParam Shipping.ShippingStatus status) {
        try {
            Shipping updatedShipping = shippingService.updateShippingStatus(id, status);
            return ResponseEntity.ok(updatedShipping);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/tracking")
    public ResponseEntity<Shipping> updateTrackingInfo(
            @PathVariable Long id,
            @RequestParam String trackingNumber,
            @RequestParam String carrier) {
        try {
            Shipping updatedShipping = shippingService.updateTrackingInfo(id, trackingNumber, carrier);
            return ResponseEntity.ok(updatedShipping);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShipping(@PathVariable Long id) {
        shippingService.deleteShipping(id);
        return ResponseEntity.noContent().build();
    }
}