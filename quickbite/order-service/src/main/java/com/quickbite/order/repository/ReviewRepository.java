package com.quickbite.order.repository;

import com.quickbite.order.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByRestaurantId(Long restaurantId);
    List<Review> findByRestaurantIdAndEntityType(Long restaurantId, String entityType);
    List<Review> findByDeliveryAgentId(Long deliveryAgentId);
    List<Review> findByDeliveryAgentIdAndEntityType(Long deliveryAgentId, String entityType);
    List<Review> findByCustomerId(Long customerId);
}
