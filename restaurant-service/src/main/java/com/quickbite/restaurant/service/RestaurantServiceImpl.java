package com.quickbite.restaurant.service;

import com.quickbite.restaurant.dto.RestaurantRequest;
import com.quickbite.restaurant.dto.RestaurantResponse;
import com.quickbite.restaurant.entity.Restaurant;
import com.quickbite.restaurant.entity.RestaurantStatus;
import com.quickbite.restaurant.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestTemplate restTemplate;

    public RestaurantServiceImpl(RestaurantRepository restaurantRepository, RestTemplate restTemplate) {
        this.restaurantRepository = restaurantRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public RestaurantResponse registerRestaurant(RestaurantRequest request) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .cuisine(request.getCuisine())
                .location(request.getLocation())
                .ownerId(request.getOwnerId())
                .status(RestaurantStatus.PENDING)
                .rating(0.0)
                .build();

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return mapToResponse(savedRestaurant);
    }

    @Override
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        
        restaurant.setName(request.getName());
        restaurant.setCuisine(request.getCuisine());
        restaurant.setLocation(request.getLocation());
        
        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        return mapToResponse(updatedRestaurant);
    }

    @Override
    public RestaurantResponse approveRestaurant(Long id, boolean isApproved) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        restaurant.setStatus(isApproved ? RestaurantStatus.APPROVED : RestaurantStatus.REJECTED);
        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);

        // Send Notification to Owner
        try {
            Map<String, Object> notificationRequest = new HashMap<>();
            notificationRequest.put("customerId", restaurant.getOwnerId());
            notificationRequest.put("type", "SYSTEM");
            notificationRequest.put("message", isApproved ? 
                "Congratulations! Your restaurant '" + restaurant.getName() + "' has been APPROVED and is now live." :
                "We regret to inform you that your restaurant '" + restaurant.getName() + "' request has been REJECTED.");

            restTemplate.postForEntity("http://notification-service/api/notifications", notificationRequest, String.class);
        } catch (Exception e) {
            System.err.println("Failed to send approval notification: " + e.getMessage());
        }

        return mapToResponse(updatedRestaurant);
    }

    @Override
    public List<RestaurantResponse> searchRestaurants(String keyword, String cuisine) {
        List<Restaurant> restaurants;
        
        if (cuisine != null && !cuisine.isEmpty()) {
            restaurants = restaurantRepository.findByStatusAndCuisineContainingIgnoreCase(RestaurantStatus.APPROVED, cuisine);
        } else if (keyword != null && !keyword.isEmpty()) {
            restaurants = restaurantRepository.findByStatusAndNameContainingIgnoreCase(RestaurantStatus.APPROVED, keyword);
        } else {
            restaurants = restaurantRepository.findByStatus(RestaurantStatus.APPROVED);
        }

        return restaurants.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public RestaurantResponse getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        return mapToResponse(restaurant);
    }

    @Override
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteRestaurant(Long id) {
        if (!restaurantRepository.existsById(id)) {
            throw new RuntimeException("Restaurant not found");
        }
        restaurantRepository.deleteById(id);
    }

    private RestaurantResponse mapToResponse(Restaurant restaurant) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .cuisine(restaurant.getCuisine())
                .location(restaurant.getLocation())
                .rating(restaurant.getRating())
                .status(restaurant.getStatus())
                .ownerId(restaurant.getOwnerId())
                .build();
    }
}
