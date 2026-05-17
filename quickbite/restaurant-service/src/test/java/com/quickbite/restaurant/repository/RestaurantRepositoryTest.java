package com.quickbite.restaurant.repository;

import com.quickbite.restaurant.entity.Restaurant;
import com.quickbite.restaurant.entity.RestaurantStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class RestaurantRepositoryTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Test
    public void testFindByStatus() {
        // Arrange
        Restaurant approvedRest = Restaurant.builder()
                .name("Pizza Hut")
                .cuisine("Italian")
                .location("Downtown")
                .ownerId(1L)
                .status(RestaurantStatus.APPROVED)
                .build();
                
        Restaurant pendingRest = Restaurant.builder()
                .name("Burger King")
                .cuisine("American")
                .location("Uptown")
                .ownerId(2L)
                .status(RestaurantStatus.PENDING)
                .build();

        restaurantRepository.save(approvedRest);
        restaurantRepository.save(pendingRest);

        // Act
        List<Restaurant> approvedRestaurants = restaurantRepository.findByStatus(RestaurantStatus.APPROVED);

        // Assert
        assertThat(approvedRestaurants).hasSize(1);
        assertThat(approvedRestaurants.get(0).getName()).isEqualTo("Pizza Hut");
    }

    @Test
    public void testFindByOwnerId() {
        // Arrange
        Restaurant rest = Restaurant.builder()
                .name("Subway")
                .cuisine("Healthy")
                .location("Midtown")
                .ownerId(5L)
                .status(RestaurantStatus.APPROVED)
                .build();
        restaurantRepository.save(rest);

        // Act
        Optional<Restaurant> foundRest = restaurantRepository.findByOwnerId(5L);

        // Assert
        assertThat(foundRest).isPresent();
        assertThat(foundRest.get().getName()).isEqualTo("Subway");
    }
}
