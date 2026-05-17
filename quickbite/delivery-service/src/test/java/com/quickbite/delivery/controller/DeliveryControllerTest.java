package com.quickbite.delivery.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.delivery.dto.AgentRequest;
import com.quickbite.delivery.dto.AgentResponse;
import com.quickbite.delivery.service.DeliveryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeliveryController.class)
public class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeliveryService deliveryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRegisterAgent() throws Exception {
        AgentRequest request = new AgentRequest();
        request.setName("John");
        request.setPhone("123");
        request.setUserId(1L);

        AgentResponse response = AgentResponse.builder()
                .id(1L)
                .name("John")
                .isAvailable(true)
                .build();

        when(deliveryService.registerAgent(any(AgentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/delivery/agents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John"));
    }

    @Test
    public void testAssignOrder() throws Exception {
        Long orderId = 501L;

        AgentResponse response = AgentResponse.builder()
                .id(1L)
                .isAvailable(false)
                .currentOrderId(orderId)
                .build();

        when(deliveryService.assignOrder(orderId)).thenReturn(response);

        mockMvc.perform(post("/api/delivery/assign/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAvailable").value(false))
                .andExpect(jsonPath("$.currentOrderId").value(orderId));
    }
}
