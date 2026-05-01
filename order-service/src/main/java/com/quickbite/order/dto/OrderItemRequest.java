package com.quickbite.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
    private Long menuItemId;
    private String menuItemName;
    private Integer quantity;
    private Double price;
}
