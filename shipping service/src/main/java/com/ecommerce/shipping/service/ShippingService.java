package com.ecommerce.shipping.service;

import com.ecommerce.shipping.model.Shipping;
import com.ecommerce.shipping.repository.ShippingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ShippingService {
    
    @Autowired
    private ShippingRepository shippingRepository;
    
    public Shipping createShipping(Shipping shipping) {
        return shippingRepository.save(shipping);
    }
    
    public List<Shipping> getAllShippings() {
        return shippingRepository.findAll();
    }
    
    public Optional<Shipping> getShippingById(Long id) {
        return shippingRepository.findById(id);
    }
    
    public List<Shipping> getShippingsByOrderId(Long orderId) {
        return shippingRepository.findByOrderId(orderId);
    }
    
    public List<Shipping> getShippingsByCustomerId(Long customerId) {
        return shippingRepository.findByCustomerId(customerId);
    }
    
    public Optional<Shipping> getShippingByTrackingNumber(String trackingNumber) {
        return shippingRepository.findByTrackingNumber(trackingNumber);
    }
    
    public Shipping updateShippingStatus(Long id, Shipping.ShippingStatus status) {
        Shipping shipping = shippingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipping not found with id: " + id));
        shipping.setStatus(status);
        return shippingRepository.save(shipping);
    }
    
    public Shipping updateTrackingInfo(Long id, String trackingNumber, String carrier) {
        Shipping shipping = shippingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipping not found with id: " + id));
        shipping.setTrackingNumber(trackingNumber);
        shipping.setCarrier(carrier);
        shipping.setStatus(Shipping.ShippingStatus.SHIPPED);
        return shippingRepository.save(shipping);
    }
    
    public void deleteShipping(Long id) {
        shippingRepository.deleteById(id);
    }
}