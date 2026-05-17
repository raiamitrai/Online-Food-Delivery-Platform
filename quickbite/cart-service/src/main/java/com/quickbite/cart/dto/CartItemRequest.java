package com.quickbite.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequest {
    private Long menuItemId;
    private String menuItemName;
    private Long restaurantId;
    private Integer quantity;
    private Double price;
}
