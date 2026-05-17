package com.quickbite.cart.service;

import com.quickbite.cart.dto.CartItemRequest;
import com.quickbite.cart.dto.CartItemResponse;
import com.quickbite.cart.dto.CartResponse;
import com.quickbite.cart.dto.PromoRequest;
import com.quickbite.cart.entity.Cart;
import com.quickbite.cart.entity.CartItem;
import com.quickbite.cart.repository.CartItemRepository;
import com.quickbite.cart.repository.CartRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public CartResponse addItemToCart(Long customerId, CartItemRequest request) {
        Cart cart = cartRepository.findByCustomerId(customerId).orElseGet(() -> {
            Cart newCart = Cart.builder()
                    .customerId(customerId)
                    .restaurantId(request.getRestaurantId())
                    .build();
            return cartRepository.save(newCart);
        });

        // Single Restaurant Constraint
        if (cart.getRestaurantId() != null && !cart.getRestaurantId().equals(request.getRestaurantId())) {
            throw new RuntimeException("Cannot add items from multiple restaurants. Please clear your cart first.");
        }
        
        cart.setRestaurantId(request.getRestaurantId());

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getMenuItemId().equals(request.getMenuItemId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .menuItemId(request.getMenuItemId())
                    .menuItemName(request.getMenuItemName())
                    .price(request.getPrice())
                    .quantity(request.getQuantity())
                    .build();
            cart.addItem(newItem);
        }

        calculateTotal(cart);
        Cart savedCart = cartRepository.save(cart);
        return mapToResponse(savedCart);
    }

    @Override
    public CartResponse removeItemFromCart(Long customerId, Long itemId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        cart.removeItem(itemToRemove);
        cartItemRepository.delete(itemToRemove);

        if (cart.getItems().isEmpty()) {
            cart.setRestaurantId(null);
            cart.setPromoCode(null);
            cart.setDiscount(0.0);
        }

        calculateTotal(cart);
        Cart savedCart = cartRepository.save(cart);
        return mapToResponse(savedCart);
    }

    @Override
    public CartResponse applyPromoCode(Long customerId, PromoRequest request) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Dummy promo logic
        if ("DISCOUNT10".equalsIgnoreCase(request.getPromoCode())) {
            cart.setPromoCode(request.getPromoCode());
            cart.setDiscount(10.0); // 10% discount
        } else {
            throw new RuntimeException("Invalid promo code");
        }

        calculateTotal(cart);
        Cart savedCart = cartRepository.save(cart);
        return mapToResponse(savedCart);
    }

    @Override
    public CartResponse getCartByCustomerId(Long customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> Cart.builder().customerId(customerId).build());
        return mapToResponse(cart);
    }

    @Override
    public void clearCart(Long customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        cart.getItems().clear();
        cart.setRestaurantId(null);
        cart.setPromoCode(null);
        cart.setDiscount(0.0);
        calculateTotal(cart);
        cartRepository.save(cart);
    }

    private void calculateTotal(Cart cart) {
        double subtotal = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        
        double discountAmount = (subtotal * cart.getDiscount()) / 100;
        cart.setTotalAmount(subtotal - discountAmount);
    }

    private CartResponse mapToResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems() != null ? 
                cart.getItems().stream().map(item -> CartItemResponse.builder()
                        .id(item.getId())
                        .menuItemId(item.getMenuItemId())
                        .menuItemName(item.getMenuItemName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .build()).collect(Collectors.toList()) 
                : List.of();

        return CartResponse.builder()
                .id(cart.getId())
                .customerId(cart.getCustomerId())
                .restaurantId(cart.getRestaurantId())
                .promoCode(cart.getPromoCode())
                .discount(cart.getDiscount())
                .totalAmount(cart.getTotalAmount())
                .items(items)
                .build();
    }
}
