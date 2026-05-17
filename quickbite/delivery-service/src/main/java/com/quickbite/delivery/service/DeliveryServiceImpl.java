package com.quickbite.delivery.service;

import com.quickbite.delivery.dto.AgentRequest;
import com.quickbite.delivery.dto.AgentResponse;
import com.quickbite.delivery.dto.LocationUpdateRequest;
import com.quickbite.delivery.entity.DeliveryAgent;
import com.quickbite.delivery.repository.DeliveryAgentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryAgentRepository deliveryAgentRepository;

    public DeliveryServiceImpl(DeliveryAgentRepository deliveryAgentRepository) {
        this.deliveryAgentRepository = deliveryAgentRepository;
    }

    @Override
    public AgentResponse registerAgent(AgentRequest request) {
        DeliveryAgent agent = DeliveryAgent.builder()
                .userId(request.getUserId())
                .name(request.getName())
                .phone(request.getPhone())
                .currentLocation(request.getInitialLocation())
                .isAvailable(true)
                .build();
        DeliveryAgent savedAgent = deliveryAgentRepository.save(agent);
        return mapToResponse(savedAgent);
    }

    @Override
    public AgentResponse updateLocation(Long agentId, LocationUpdateRequest request) {
        DeliveryAgent agent = deliveryAgentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        agent.setCurrentLocation(request.getLocation());
        DeliveryAgent updatedAgent = deliveryAgentRepository.save(agent);
        return mapToResponse(updatedAgent);
    }

    @Override
    public AgentResponse assignOrder(Long orderId) {
        List<DeliveryAgent> availableAgents = deliveryAgentRepository.findByIsAvailableTrue();
        
        if(availableAgents.isEmpty()) {
            throw new RuntimeException("No delivery agents available right now");
        }

        // Dummy logic: Assign to the first available agent. 
        // In real world, we would calculate distance using currentLocation
        DeliveryAgent agent = availableAgents.get(0);
        agent.setIsAvailable(false);
        agent.setCurrentOrderId(orderId);
        
        DeliveryAgent updatedAgent = deliveryAgentRepository.save(agent);
        return mapToResponse(updatedAgent);
    }

    @Override
    public AgentResponse markDelivered(Long agentId, Long orderId) {
        DeliveryAgent agent = deliveryAgentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        if (!orderId.equals(agent.getCurrentOrderId())) {
            throw new RuntimeException("Agent is not assigned to this order");
        }

        agent.setIsAvailable(true);
        agent.setCurrentOrderId(null);
        DeliveryAgent updatedAgent = deliveryAgentRepository.save(agent);
        
        // Note: Realistically, this would send an event to Order Service to mark status = DELIVERED
        return mapToResponse(updatedAgent);
    }

    @Override
    public AgentResponse getAgentStatus(Long agentId) {
        DeliveryAgent agent = deliveryAgentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        return mapToResponse(agent);
    }

    @Override
    public AgentResponse getAgentByUserId(Long userId) {
        DeliveryAgent agent = deliveryAgentRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Agent not found for user"));
        return mapToResponse(agent);
    }

    @Override
    public AgentResponse updateAvailability(Long agentId, boolean isAvailable) {
        DeliveryAgent agent = deliveryAgentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        agent.setIsAvailable(isAvailable);
        DeliveryAgent updatedAgent = deliveryAgentRepository.save(agent);
        return mapToResponse(updatedAgent);
    }

    @Override
    public List<AgentResponse> getAllAgents() {
        return deliveryAgentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AgentResponse mapToResponse(DeliveryAgent agent) {
        return AgentResponse.builder()
                .id(agent.getId())
                .userId(agent.getUserId())
                .name(agent.getName())
                .phone(agent.getPhone())
                .currentLocation(agent.getCurrentLocation())
                .isAvailable(agent.getIsAvailable())
                .currentOrderId(agent.getCurrentOrderId())
                .build();
    }
}
