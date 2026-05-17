package com.quickbite.order.service;

import com.quickbite.order.dto.OrderRequest;
import com.quickbite.order.dto.OrderResponse;
import com.quickbite.order.entity.Order;
import com.quickbite.order.entity.OrderStatus;
import com.quickbite.order.producer.NotificationProducer;
import com.quickbite.order.client.RestaurantClient;
import com.quickbite.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private NotificationProducer notificationProducer;

    @Mock
    private RestaurantClient restaurantClient;

    @InjectMocks
    private OrderServiceImpl orderService;



    @Test
    public void testPlaceOrder_Success() {
        // Arrange
        Long customerId = 101L;
        OrderRequest request = new OrderRequest();
        request.setRestaurantId(201L);
        request.setTotalAmount(500.0);
        // Can add items to request if needed

        Order savedOrder = Order.builder()
                .id(1L)
                .customerId(customerId)
                .restaurantId(request.getRestaurantId())
                .totalAmount(request.getTotalAmount())
                .status(OrderStatus.PLACED)
                .build();

        when(orderRepository.save(any())).thenReturn(savedOrder);
        when(restaurantClient.getRestaurantById(any())).thenReturn(Map.of("ownerId", 301L));

        // Act
        OrderResponse response = orderService.placeOrder(customerId, request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(OrderStatus.PLACED, response.getStatus());
        assertEquals(500.0, response.getTotalAmount());
        
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(notificationProducer, times(2)).sendNotification(anyMap()); // Customer and Restaurant
    }

    @Test
    public void testUpdateOrderStatus_Success() {
        // Arrange
        Order existingOrder = Order.builder()
                .id(1L)
                .customerId(101L)
                .status(OrderStatus.PLACED)
                .build();

        when(orderRepository.findById(any())).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponse response = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

        // Assert
        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
        verify(notificationProducer, times(1)).sendNotification(anyMap());
    }

    @Test
    public void testCancelOrder_InvalidStage_ThrowsException() {
        // Arrange
        Order existingOrder = Order.builder()
                .id(1L)
                .status(OrderStatus.PREPARING) // Cannot cancel if preparing
                .build();

        when(orderRepository.findById(any())).thenReturn(Optional.of(existingOrder));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.cancelOrder(1L);
        });

        assertEquals("Cannot cancel order at this stage", exception.getMessage());
    }
}
