package com.quickbite.cart.controller;

import com.quickbite.cart.dto.CartItemRequest;
import com.quickbite.cart.dto.CartResponse;
import com.quickbite.cart.dto.PromoRequest;
import com.quickbite.cart.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/{customerId}/items")
    public ResponseEntity<CartResponse> addItemToCart(@PathVariable Long customerId, @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addItemToCart(customerId, request));
    }

    @DeleteMapping("/{customerId}/items/{itemId}")
    public ResponseEntity<CartResponse> removeItemFromCart(@PathVariable Long customerId, @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(customerId, itemId));
    }

    @PostMapping("/{customerId}/promo")
    public ResponseEntity<CartResponse> applyPromoCode(@PathVariable Long customerId, @RequestBody PromoRequest request) {
        return ResponseEntity.ok(cartService.applyPromoCode(customerId, request));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable Long customerId) {
        return ResponseEntity.ok(cartService.getCartByCustomerId(customerId));
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long customerId) {
        cartService.clearCart(customerId);
        return ResponseEntity.noContent().build();
    }
}
