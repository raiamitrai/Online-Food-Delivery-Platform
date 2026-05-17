package com.quickbite.review.service;

import com.quickbite.review.dto.ReviewRequest;
import com.quickbite.review.dto.ReviewResponse;
import com.quickbite.review.entity.Review;
import com.quickbite.review.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public ReviewResponse submitReview(ReviewRequest request) {
        if (reviewRepository.existsByOrderId(request.getOrderId())) {
            throw new RuntimeException("A review already exists for this order.");
        }

        Review review = Review.builder()
                .customerId(request.getCustomerId())
                .orderId(request.getOrderId())
                .restaurantId(request.getRestaurantId())
                .deliveryAgentId(request.getDeliveryAgentId())
                .restaurantRating(request.getRestaurantRating())
                .agentRating(request.getAgentRating())
                .comments(request.getComments())
                .createdAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);
        return mapToResponse(savedReview);
    }

    @Override
    public List<ReviewResponse> getReviewsByRestaurant(Long restaurantId) {
        return reviewRepository.findByRestaurantId(restaurantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> getReviewsByAgent(Long agentId) {
        return reviewRepository.findByDeliveryAgentId(agentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .customerId(review.getCustomerId())
                .orderId(review.getOrderId())
                .restaurantId(review.getRestaurantId())
                .deliveryAgentId(review.getDeliveryAgentId())
                .restaurantRating(review.getRestaurantRating())
                .agentRating(review.getAgentRating())
                .comments(review.getComments())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
