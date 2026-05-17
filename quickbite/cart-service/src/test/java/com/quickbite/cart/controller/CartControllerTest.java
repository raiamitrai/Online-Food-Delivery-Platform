package com.quickbite.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.cart.dto.CartItemRequest;
import com.quickbite.cart.dto.CartResponse;
import com.quickbite.cart.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetCart() throws Exception {
        Long customerId = 1L;
        CartResponse response = CartResponse.builder()
                .customerId(customerId)
                .totalAmount(0.0)
                .build();

        when(cartService.getCartByCustomerId(customerId)).thenReturn(response);

        mockMvc.perform(get("/api/cart/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(customerId));
    }

    @Test
    public void testAddItemToCart() throws Exception {
        Long customerId = 1L;
        CartItemRequest request = new CartItemRequest();
        request.setRestaurantId(101L);
        request.setMenuItemId(201L);
        request.setQuantity(1);

        CartResponse response = CartResponse.builder()
                .customerId(customerId)
                .restaurantId(101L)
                .totalAmount(200.0)
                .build();

        when(cartService.addItemToCart(eq(customerId), any(CartItemRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/cart/{customerId}/items", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(200.0));
    }
}
