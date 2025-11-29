package com.ecommerce.notification.repository;

import com.ecommerce.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByCustomerId(Long customerId);
    List<Notification> findByOrderId(Long orderId);
    List<Notification> findByType(Notification.NotificationType type);
    List<Notification> findByStatus(Notification.NotificationStatus status);
}