package com.quickbite.order.dto;

import com.quickbite.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private Long customerId;
    private Long restaurantId;
    private Long deliveryAgentId;
    private OrderStatus status;
    private Double totalAmount;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}
