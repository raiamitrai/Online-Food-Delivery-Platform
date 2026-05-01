package com.quickbite.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

@FeignClient(name = "delivery-service")
public interface DeliveryClient {
    @GetMapping("/api/delivery/agents/{id}")
    Map<String, Object> getAgentById(@PathVariable("id") Long id);
}
