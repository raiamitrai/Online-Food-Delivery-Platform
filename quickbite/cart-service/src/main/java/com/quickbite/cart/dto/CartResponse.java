package com.quickbite.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private Long id;
    private Long customerId;
    private Long restaurantId;
    private String promoCode;
    private Double discount;
    private Double totalAmount;
    private List<CartItemResponse> items;
}
