package com.quickbite.delivery.controller;

import com.quickbite.delivery.dto.AgentRequest;
import com.quickbite.delivery.dto.AgentResponse;
import com.quickbite.delivery.dto.LocationUpdateRequest;
import com.quickbite.delivery.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping("/agents")
    public ResponseEntity<AgentResponse> registerAgent(@RequestBody AgentRequest request) {
        return ResponseEntity.ok(deliveryService.registerAgent(request));
    }

    @PutMapping("/agents/{id}/location")
    public ResponseEntity<AgentResponse> updateLocation(@PathVariable Long id, @RequestBody LocationUpdateRequest request) {
        return ResponseEntity.ok(deliveryService.updateLocation(id, request));
    }

    @PostMapping("/assign/{orderId}")
    public ResponseEntity<AgentResponse> assignOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(deliveryService.assignOrder(orderId));
    }

    @PutMapping("/agents/{id}/deliver/{orderId}")
    public ResponseEntity<AgentResponse> markDelivered(@PathVariable Long id, @PathVariable Long orderId) {
        return ResponseEntity.ok(deliveryService.markDelivered(id, orderId));
    }

    @GetMapping("/agents/{id}")
    public ResponseEntity<AgentResponse> getAgentStatus(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.getAgentStatus(id));
    }

    @GetMapping("/agents/user/{userId}")
    public ResponseEntity<AgentResponse> getAgentByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(deliveryService.getAgentByUserId(userId));
    }

    @PutMapping("/agents/{id}/availability")
    public ResponseEntity<AgentResponse> updateAvailability(@PathVariable Long id, @RequestParam boolean isAvailable) {
        return ResponseEntity.ok(deliveryService.updateAvailability(id, isAvailable));
    }

    @GetMapping("/agents")
    public ResponseEntity<java.util.List<AgentResponse>> getAllAgents() {
        return ResponseEntity.ok(deliveryService.getAllAgents());
    }
}
