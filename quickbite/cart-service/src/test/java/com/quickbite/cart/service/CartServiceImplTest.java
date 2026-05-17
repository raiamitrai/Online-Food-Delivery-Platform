package com.quickbite.cart.service;

import com.quickbite.cart.dto.CartItemRequest;
import com.quickbite.cart.dto.CartResponse;
import com.quickbite.cart.entity.Cart;
import com.quickbite.cart.repository.CartItemRepository;
import com.quickbite.cart.repository.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    public void testAddItemToCart_NewCart_Success() {
        // Arrange
        Long customerId = 1L;
        CartItemRequest request = new CartItemRequest();
        request.setRestaurantId(101L);
        request.setMenuItemId(201L);
        request.setMenuItemName("Pizza");
        request.setPrice(300.0);
        request.setQuantity(2);

        Cart cart = Cart.builder()
                .customerId(customerId)
                .restaurantId(101L)
                .build();

        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CartResponse response = cartService.addItemToCart(customerId, request);

        // Assert
        assertNotNull(response);
        assertEquals(customerId, response.getCustomerId());
        assertEquals(101L, response.getRestaurantId());
        assertEquals(600.0, response.getTotalAmount());
        assertEquals(1, response.getItems().size());
    }

    @Test
    public void testAddItemToCart_DifferentRestaurant_ThrowsException() {
        // Arrange
        Long customerId = 1L;
        CartItemRequest request = new CartItemRequest();
        request.setRestaurantId(102L); // Different restaurant
        
        Cart existingCart = Cart.builder()
                .customerId(customerId)
                .restaurantId(101L) // Existing restaurant
                .build();

        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(existingCart));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            cartService.addItemToCart(customerId, request);
        });

        assertEquals("Cannot add items from multiple restaurants. Please clear your cart first.", exception.getMessage());
    }
}
