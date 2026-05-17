package com.quickbite.delivery.service;

import com.quickbite.delivery.dto.AgentRequest;
import com.quickbite.delivery.dto.AgentResponse;
import com.quickbite.delivery.entity.DeliveryAgent;
import com.quickbite.delivery.repository.DeliveryAgentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeliveryServiceImplTest {

    @Mock
    private DeliveryAgentRepository deliveryAgentRepository;

    @InjectMocks
    private DeliveryServiceImpl deliveryService;

    @Test
    public void testRegisterAgent_Success() {
        // Arrange
        AgentRequest request = new AgentRequest();
        request.setUserId(10L);
        request.setName("John");
        request.setPhone("12345");
        request.setInitialLocation("PointA");

        DeliveryAgent savedAgent = DeliveryAgent.builder()
                .id(1L)
                .userId(10L)
                .name("John")
                .phone("12345")
                .currentLocation("PointA")
                .isAvailable(true)
                .build();

        when(deliveryAgentRepository.save(any(DeliveryAgent.class))).thenReturn(savedAgent);

        // Act
        AgentResponse response = deliveryService.registerAgent(request);

        // Assert
        assertNotNull(response);
        assertEquals("John", response.getName());
        assertTrue(response.getIsAvailable());
    }

    @Test
    public void testAssignOrder_Success() {
        // Arrange
        Long orderId = 501L;
        DeliveryAgent agent = DeliveryAgent.builder()
                .id(1L)
                .isAvailable(true)
                .build();

        when(deliveryAgentRepository.findByIsAvailableTrue()).thenReturn(List.of(agent));
        when(deliveryAgentRepository.save(any(DeliveryAgent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AgentResponse response = deliveryService.assignOrder(orderId);

        // Assert
        assertFalse(response.getIsAvailable());
        assertEquals(orderId, response.getCurrentOrderId());
    }

    @Test
    public void testAssignOrder_NoAgentsAvailable_ThrowsException() {
        // Arrange
        when(deliveryAgentRepository.findByIsAvailableTrue()).thenReturn(List.of());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            deliveryService.assignOrder(501L);
        });

        assertEquals("No delivery agents available right now", exception.getMessage());
    }

    @Test
    public void testMarkDelivered_Success() {
        // Arrange
        Long agentId = 1L;
        Long orderId = 501L;

        DeliveryAgent agent = DeliveryAgent.builder()
                .id(agentId)
                .isAvailable(false)
                .currentOrderId(orderId)
                .build();

        when(deliveryAgentRepository.findById(agentId)).thenReturn(Optional.of(agent));
        when(deliveryAgentRepository.save(any(DeliveryAgent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AgentResponse response = deliveryService.markDelivered(agentId, orderId);

        // Assert
        assertTrue(response.getIsAvailable());
        assertNull(response.getCurrentOrderId());
    }
}
