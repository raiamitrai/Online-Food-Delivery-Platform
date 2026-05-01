package com.quickbite.cart.service;

import com.quickbite.cart.dto.CartItemRequest;
import com.quickbite.cart.dto.CartResponse;
import com.quickbite.cart.dto.PromoRequest;

public interface CartService {
    CartResponse addItemToCart(Long customerId, CartItemRequest request);
    CartResponse removeItemFromCart(Long customerId, Long itemId);
    CartResponse applyPromoCode(Long customerId, PromoRequest request);
    CartResponse getCartByCustomerId(Long customerId);
    void clearCart(Long customerId);
}
