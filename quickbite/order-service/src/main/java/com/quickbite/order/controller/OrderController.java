package com.quickbite.order.controller;

import com.quickbite.order.dto.OrderRequest;
import com.quickbite.order.dto.OrderResponse;
import com.quickbite.order.entity.OrderStatus;
import com.quickbite.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/{customerId}")
    public ResponseEntity<OrderResponse> placeOrder(@PathVariable Long customerId, @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.placeOrder(customerId, request));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long orderId, @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }

    @PutMapping("/{orderId}/assign/{agentId}")
    public ResponseEntity<OrderResponse> assignDeliveryAgent(@PathVariable Long orderId, @PathVariable Long agentId) {
        return ResponseEntity.ok(orderService.assignDeliveryAgent(orderId, agentId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomerId(customerId));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByRestaurantId(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(orderService.getOrdersByRestaurantId(restaurantId));
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByDeliveryAgentId(@PathVariable Long agentId) {
        return ResponseEntity.ok(orderService.getOrdersByDeliveryAgentId(agentId));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }
}
