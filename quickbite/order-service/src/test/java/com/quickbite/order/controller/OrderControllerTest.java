package com.quickbite.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.order.dto.OrderRequest;
import com.quickbite.order.dto.OrderResponse;
import com.quickbite.order.entity.OrderStatus;
import com.quickbite.order.service.OrderService;
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

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testPlaceOrder() throws Exception {
        Long customerId = 101L;
        OrderRequest request = new OrderRequest();
        request.setRestaurantId(201L);
        request.setTotalAmount(500.0);

        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .customerId(customerId)
                .restaurantId(request.getRestaurantId())
                .totalAmount(request.getTotalAmount())
                .status(OrderStatus.PLACED)
                .build();

        when(orderService.placeOrder(eq(customerId), any(OrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/orders/{customerId}", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andExpect(jsonPath("$.status").value("PLACED"));
    }

    @Test
    public void testGetOrderById() throws Exception {
        Long orderId = 1L;
        OrderResponse response = OrderResponse.builder()
                .id(orderId)
                .customerId(101L)
                .status(OrderStatus.CONFIRMED)
                .build();

        when(orderService.getOrderById(orderId)).thenReturn(response);

        mockMvc.perform(get("/api/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }
}
