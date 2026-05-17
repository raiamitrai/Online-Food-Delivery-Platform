package com.quickbite.review.service;

import com.quickbite.review.dto.ReviewRequest;
import com.quickbite.review.dto.ReviewResponse;

import java.util.List;

public interface ReviewService {
    ReviewResponse submitReview(ReviewRequest request);
    List<ReviewResponse> getReviewsByRestaurant(Long restaurantId);
    List<ReviewResponse> getReviewsByAgent(Long agentId);
    List<ReviewResponse> getAllReviews();
}
