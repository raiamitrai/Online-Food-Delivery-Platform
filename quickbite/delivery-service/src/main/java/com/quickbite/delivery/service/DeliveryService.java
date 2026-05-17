package com.quickbite.delivery.service;

import com.quickbite.delivery.dto.AgentRequest;
import com.quickbite.delivery.dto.AgentResponse;
import com.quickbite.delivery.dto.LocationUpdateRequest;

public interface DeliveryService {
    AgentResponse registerAgent(AgentRequest request);
    AgentResponse updateLocation(Long agentId, LocationUpdateRequest request);
    AgentResponse assignOrder(Long orderId);
    AgentResponse markDelivered(Long agentId, Long orderId);
    AgentResponse getAgentStatus(Long agentId);
    AgentResponse getAgentByUserId(Long userId);
    AgentResponse updateAvailability(Long agentId, boolean isAvailable);
    java.util.List<AgentResponse> getAllAgents();
}
