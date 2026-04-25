package com.quickbite.restaurant.dto;

import com.quickbite.restaurant.entity.RestaurantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {
    private Long id;
    private String name;
    private String cuisine;
    private String location;
    private Double rating;
    private RestaurantStatus status;
    private Long ownerId;
}
