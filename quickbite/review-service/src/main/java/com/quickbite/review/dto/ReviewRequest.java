package com.quickbite.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {
    private Long customerId;
    private Long orderId;
    private Long restaurantId;
    private Long deliveryAgentId;
    private Integer restaurantRating;
    private Integer agentRating;
    private String comments;
}
