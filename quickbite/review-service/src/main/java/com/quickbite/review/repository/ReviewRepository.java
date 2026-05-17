package com.quickbite.review.repository;

import com.quickbite.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByRestaurantId(Long restaurantId);
    List<Review> findByDeliveryAgentId(Long deliveryAgentId);
    boolean existsByOrderId(Long orderId);
}
