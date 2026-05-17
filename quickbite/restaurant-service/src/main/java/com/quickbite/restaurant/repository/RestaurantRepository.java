package com.quickbite.restaurant.repository;

import com.quickbite.restaurant.entity.Restaurant;
import com.quickbite.restaurant.entity.RestaurantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByStatus(RestaurantStatus status);
    List<Restaurant> findByStatusAndCuisineContainingIgnoreCase(RestaurantStatus status, String cuisine);
    List<Restaurant> findByStatusAndNameContainingIgnoreCase(RestaurantStatus status, String name);
    java.util.Optional<Restaurant> findByOwnerId(Long ownerId);
}
