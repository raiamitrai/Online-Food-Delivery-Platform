package com.quickbite.order.service;

import com.quickbite.order.dto.OrderRequest;
import com.quickbite.order.dto.OrderResponse;
import com.quickbite.order.dto.OrderItemResponse;
import com.quickbite.order.entity.Order;
import com.quickbite.order.entity.OrderItem;
import com.quickbite.order.entity.OrderStatus;
import com.quickbite.order.client.NotificationClient;
import com.quickbite.order.client.RestaurantClient;
import com.quickbite.order.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final NotificationClient notificationClient;
    private final RestaurantClient restaurantClient;

    public OrderServiceImpl(OrderRepository orderRepository, NotificationClient notificationClient, RestaurantClient restaurantClient) {
        this.orderRepository = orderRepository;
        this.notificationClient = notificationClient;
        this.restaurantClient = restaurantClient;
    }

    @Override
    public OrderResponse placeOrder(Long customerId, OrderRequest request) {
        Order order = Order.builder()
                .customerId(customerId)
                .restaurantId(request.getRestaurantId())
                .totalAmount(request.getTotalAmount())
                .status(OrderStatus.PLACED)
                .createdAt(LocalDateTime.now())
                .build();

        if (request.getItems() != null) {
            request.getItems().forEach(itemRequest -> {
                OrderItem item = OrderItem.builder()
                        .menuItemId(itemRequest.getMenuItemId())
                        .menuItemName(itemRequest.getMenuItemName())
                        .quantity(itemRequest.getQuantity())
                        .price(itemRequest.getPrice())
                        .build();
                order.addItem(item);
            });
        }

        Order savedOrder = orderRepository.save(order);
        
        // Notify Restaurant & Customer
        try {
            System.out.println(">>> Sending Customer Notification for Order #" + savedOrder.getId());
            Map<String, Object> customerNote = new HashMap<>();
            customerNote.put("customerId", customerId);
            customerNote.put("type", "ORDER_STATUS");
            customerNote.put("message", "Order #ORD-" + savedOrder.getId() + " placed successfully! Total: ₹" + savedOrder.getTotalAmount());
            notificationClient.sendNotification(customerNote);

            // To Restaurant Owner
            try {
                System.out.println(">>> Fetching Owner ID for Restaurant #" + savedOrder.getRestaurantId());
                Map<String, Object> restaurant = restaurantClient.getRestaurantById(savedOrder.getRestaurantId());
                if (restaurant != null && restaurant.containsKey("ownerId")) {
                    Long ownerId = Long.valueOf(restaurant.get("ownerId").toString());
                    System.out.println(">>> Owner ID found: " + ownerId + ". Sending Notification...");
                    Map<String, Object> restNote = new HashMap<>();
                    restNote.put("customerId", ownerId);
                    restNote.put("type", "ORDER_STATUS");
                    restNote.put("message", "New Order Received! #ORD-" + savedOrder.getId() + " for ₹" + savedOrder.getTotalAmount());
                    notificationClient.sendNotification(restNote);
                } else {
                    System.err.println(">>> Owner ID NOT found in restaurant data!");
                }
            } catch (Exception restErr) {
                System.err.println(">>> Failed to fetch restaurant owner info: " + restErr.getMessage());
            }
        } catch (Exception e) {
            System.err.println(">>> Failed to send order placement notifications: " + e.getMessage());
        }

        return mapToResponse(savedOrder);
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Basic check for cancelling
        if (status == OrderStatus.CANCELLED && 
           !(order.getStatus() == OrderStatus.PLACED || order.getStatus() == OrderStatus.CONFIRMED)) {
            throw new RuntimeException("Cannot cancel order at this stage");
        }

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        // Notify Customer about Status Change
        try {
            Map<String, Object> note = new HashMap<>();
            note.put("customerId", updatedOrder.getCustomerId());
            note.put("type", "ORDER_STATUS");
            note.put("message", "Your order #ORD-" + updatedOrder.getId() + " is now " + status + ".");
            notificationClient.sendNotification(note);
        } catch (Exception e) {
            System.err.println("Failed to send status update notification: " + e.getMessage());
        }

        return mapToResponse(updatedOrder);
    }

    @Override
    public OrderResponse cancelOrder(Long orderId) {
        return updateOrderStatus(orderId, OrderStatus.CANCELLED);
    }

    @Override
    public OrderResponse assignDeliveryAgent(Long orderId, Long agentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setDeliveryAgentId(agentId);
        if(order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.PREPARING) {
             order.setStatus(OrderStatus.PICKED_UP); // Or maybe wait till agent actually picks it up. Let's just set ID for now.
        }
        
        Order updatedOrder = orderRepository.save(order);
        return mapToResponse(updatedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return mapToResponse(order);
    }

    @Override
    public List<OrderResponse> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByRestaurantId(Long restaurantId) {
        return orderRepository.findByRestaurantId(restaurantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByDeliveryAgentId(Long agentId) {
        return orderRepository.findByDeliveryAgentId(agentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> items = order.getItems() != null ?
                order.getItems().stream().map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .menuItemId(item.getMenuItemId())
                        .menuItemName(item.getMenuItemName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build()).collect(Collectors.toList())
                : List.of();

        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .restaurantId(order.getRestaurantId())
                .deliveryAgentId(order.getDeliveryAgentId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }
}
