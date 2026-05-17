package com.quickbite.restaurant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.restaurant.dto.RestaurantRequest;
import com.quickbite.restaurant.dto.RestaurantResponse;
import com.quickbite.restaurant.entity.RestaurantStatus;
import com.quickbite.restaurant.service.RestaurantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestaurantController.class)
public class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestaurantService restaurantService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRegisterRestaurant() throws Exception {
        RestaurantRequest request = new RestaurantRequest();
        request.setName("Taco Bell");
        request.setCuisine("Mexican");

        RestaurantResponse response = RestaurantResponse.builder()
                .id(1L)
                .name("Taco Bell")
                .status(RestaurantStatus.PENDING)
                .build();

        when(restaurantService.registerRestaurant(any(RestaurantRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Taco Bell"));
    }

    @Test
    public void testSearchRestaurants() throws Exception {
        RestaurantResponse response = RestaurantResponse.builder()
                .id(1L)
                .name("Taco Bell")
                .cuisine("Mexican")
                .status(RestaurantStatus.APPROVED)
                .build();

        when(restaurantService.searchRestaurants(any(), any())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/restaurants/search")
                .param("cuisine", "Mexican"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Taco Bell"));
    }
}
