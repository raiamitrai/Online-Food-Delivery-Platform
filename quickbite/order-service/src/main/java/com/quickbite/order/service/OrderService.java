package com.quickbite.order.service;

import com.quickbite.order.dto.OrderRequest;
import com.quickbite.order.dto.OrderResponse;
import com.quickbite.order.entity.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(Long customerId, OrderRequest request);
    OrderResponse updateOrderStatus(Long orderId, OrderStatus status);
    OrderResponse cancelOrder(Long orderId);
    OrderResponse assignDeliveryAgent(Long orderId, Long agentId);
    OrderResponse getOrderById(Long orderId);
    List<OrderResponse> getOrdersByCustomerId(Long customerId);
    List<OrderResponse> getOrdersByRestaurantId(Long restaurantId);
    List<OrderResponse> getOrdersByDeliveryAgentId(Long agentId);
    List<OrderResponse> getAllOrders();
}
