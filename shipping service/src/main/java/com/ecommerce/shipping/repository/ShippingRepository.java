package com.ecommerce.shipping.repository;

import com.ecommerce.shipping.model.Shipping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingRepository extends JpaRepository<Shipping, Long> {
    List<Shipping> findByOrderId(Long orderId);
    List<Shipping> findByCustomerId(Long customerId);
    Optional<Shipping> findByTrackingNumber(String trackingNumber);
    List<Shipping> findByStatus(Shipping.ShippingStatus status);
}