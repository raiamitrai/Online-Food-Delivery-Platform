package com.quickbite.order.controller;

import com.quickbite.order.entity.Review;
import com.quickbite.order.repository.ReviewRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import com.quickbite.order.client.NotificationClient;
import com.quickbite.order.client.RestaurantClient;
import com.quickbite.order.client.DeliveryClient;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin("*")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final NotificationClient notificationClient;
    private final RestaurantClient restaurantClient;
    private final DeliveryClient deliveryClient;

    public ReviewController(ReviewRepository reviewRepository, NotificationClient notificationClient, 
                            RestaurantClient restaurantClient, DeliveryClient deliveryClient) {
        this.reviewRepository = reviewRepository;
        this.notificationClient = notificationClient;
        this.restaurantClient = restaurantClient;
        this.deliveryClient = deliveryClient;
    }

    @PostMapping
    public ResponseEntity<Review> submitReview(@RequestBody Review review) {
        System.out.println(">>> RECEIVED REVIEW REQUEST: " + review);
        review.setCreatedAt(LocalDateTime.now());
        Review saved = reviewRepository.save(review);
        System.out.println(">>> REVIEW SAVED SUCCESSFULLY WITH ID: " + saved.getId());

        // Notify Restaurant or Agent
        try {
            Map<String, Object> notify = new HashMap<>();
            String msg = "You got a new " + review.getRating() + "-star rating!";
            if (review.getComment() != null && !review.getComment().isEmpty()) {
                msg += " - " + review.getComment();
            }
            
            notify.put("message", msg);
            notify.put("type", "SYSTEM");
            
            if ("RESTAURANT".equals(review.getEntityType())) {
                Map<String, Object> rest = restaurantClient.getRestaurantById(review.getRestaurantId());
                if (rest != null && rest.containsKey("ownerId")) {
                    notify.put("customerId", rest.get("ownerId"));
                } else {
                    notify.put("customerId", review.getRestaurantId()); // Fallback
                }
            } else {
                Map<String, Object> agent = deliveryClient.getAgentById(review.getDeliveryAgentId());
                if (agent != null && agent.containsKey("userId")) {
                    notify.put("customerId", agent.get("userId"));
                } else {
                    notify.put("customerId", review.getDeliveryAgentId()); // Fallback
                }
            }
            
            notificationClient.sendNotification(notify);
            System.out.println(">>> NOTIFICATION SENT FOR REVIEW");
        } catch (Exception e) {
            System.err.println("Notification failed: " + e.getMessage());
        }

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<Review>> getReviewsByRestaurantId(@PathVariable Long restaurantId) {
        List<Review> reviews = reviewRepository.findByRestaurantIdAndEntityType(restaurantId, "RESTAURANT");
        System.out.println(">>> FETCHING REVIEWS FOR RESTAURANT " + restaurantId + ". FOUND: " + reviews.size());
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<Review>> getAgentReviews(@PathVariable Long agentId) {
        return ResponseEntity.ok(reviewRepository.findByDeliveryAgentId(agentId));
    }
}
