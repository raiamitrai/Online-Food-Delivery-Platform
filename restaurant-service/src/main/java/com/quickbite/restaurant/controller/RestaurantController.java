package com.quickbite.restaurant.controller;

import com.quickbite.restaurant.dto.RestaurantRequest;
import com.quickbite.restaurant.dto.RestaurantResponse;
import com.quickbite.restaurant.service.RestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @PostMapping
    public ResponseEntity<RestaurantResponse> registerRestaurant(@RequestBody RestaurantRequest request) {
        return ResponseEntity.ok(restaurantService.registerRestaurant(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(@PathVariable Long id, @RequestBody RestaurantRequest request) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(id, request));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<RestaurantResponse> approveRestaurant(@PathVariable Long id, @RequestParam boolean isApproved) {
        return ResponseEntity.ok(restaurantService.approveRestaurant(id, isApproved));
    }

    @GetMapping("/search")
    public ResponseEntity<List<RestaurantResponse>> searchRestaurants(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String cuisine) {
        return ResponseEntity.ok(restaurantService.searchRestaurants(keyword, cuisine));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurants() {
        return ResponseEntity.ok(restaurantService.getAllRestaurants());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.ok().build();
    }
}
