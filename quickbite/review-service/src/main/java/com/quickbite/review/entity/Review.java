package com.quickbite.review.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false, unique = true)
    private Long orderId; // Enforces One Review Per Order constraint

    @Column(nullable = false)
    private Long restaurantId;

    private Long deliveryAgentId;

    @Column(nullable = false)
    private Integer restaurantRating; // 1 to 5

    private Integer agentRating; // 1 to 5

    private String comments;

    private LocalDateTime createdAt;
}
