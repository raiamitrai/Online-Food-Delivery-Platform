package com.quickbite.review.controller;

import com.quickbite.review.dto.ReviewRequest;
import com.quickbite.review.dto.ReviewResponse;
import com.quickbite.review.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> submitReview(@RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.submitReview(request));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(reviewService.getReviewsByRestaurant(restaurantId));
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByAgent(@PathVariable Long agentId) {
        return ResponseEntity.ok(reviewService.getReviewsByAgent(agentId));
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }
}
