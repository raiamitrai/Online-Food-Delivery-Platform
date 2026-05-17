package com.quickbite.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

@FeignClient(name = "restaurant-service")
public interface RestaurantClient {
    @GetMapping("/api/restaurants/{id}")
    Map<String, Object> getRestaurantById(@PathVariable("id") Long id);
}
