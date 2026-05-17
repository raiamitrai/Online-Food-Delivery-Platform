package com.quickbite.restaurant.service;

import com.quickbite.restaurant.dto.RestaurantRequest;
import com.quickbite.restaurant.dto.RestaurantResponse;
import com.quickbite.restaurant.entity.Restaurant;
import com.quickbite.restaurant.entity.RestaurantStatus;
import com.quickbite.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantServiceImplTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RestaurantServiceImpl restaurantService;

    @Test
    public void testRegisterRestaurant_Success() {
        // Arrange
        RestaurantRequest request = new RestaurantRequest();
        request.setName("KFC");
        request.setCuisine("Fast Food");
        request.setLocation("City Center");
        request.setOwnerId(10L);

        Restaurant savedRestaurant = Restaurant.builder()
                .id(1L)
                .name("KFC")
                .cuisine("Fast Food")
                .location("City Center")
                .ownerId(10L)
                .status(RestaurantStatus.PENDING)
                .rating(0.0)
                .build();

        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(savedRestaurant);

        // Act
        RestaurantResponse response = restaurantService.registerRestaurant(request);

        // Assert
        assertNotNull(response);
        assertEquals("KFC", response.getName());
        assertEquals(RestaurantStatus.PENDING, response.getStatus());
    }

    @Test
    public void testApproveRestaurant_Success() {
        // Arrange
        Long id = 1L;
        Restaurant restaurant = Restaurant.builder()
                .id(id)
                .name("KFC")
                .ownerId(10L)
                .status(RestaurantStatus.PENDING)
                .build();

        when(restaurantRepository.findById(id)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(ResponseEntity.ok().build());

        // Act
        RestaurantResponse response = restaurantService.approveRestaurant(id, true);

        // Assert
        assertEquals(RestaurantStatus.APPROVED, response.getStatus());
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }
}
