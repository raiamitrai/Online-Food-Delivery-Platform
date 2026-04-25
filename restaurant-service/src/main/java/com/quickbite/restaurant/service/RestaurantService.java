package com.quickbite.restaurant.service;

import com.quickbite.restaurant.dto.RestaurantRequest;
import com.quickbite.restaurant.dto.RestaurantResponse;

import java.util.List;

public interface RestaurantService {
    RestaurantResponse registerRestaurant(RestaurantRequest request);
    RestaurantResponse updateRestaurant(Long id, RestaurantRequest request);
    RestaurantResponse approveRestaurant(Long id, boolean isApproved);
    List<RestaurantResponse> searchRestaurants(String keyword, String cuisine);
    RestaurantResponse getRestaurantById(Long id);
    List<RestaurantResponse> getAllRestaurants();
    void deleteRestaurant(Long id);
}
