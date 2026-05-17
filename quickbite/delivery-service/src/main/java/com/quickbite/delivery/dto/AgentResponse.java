package com.quickbite.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponse {
    private Long id;
    private Long userId;
    private String name;
    private String phone;
    private String currentLocation;
    private Boolean isAvailable;
    private Long currentOrderId;
}
