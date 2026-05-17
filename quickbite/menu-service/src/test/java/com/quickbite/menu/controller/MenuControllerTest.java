package com.quickbite.menu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.menu.dto.MenuItemRequest;
import com.quickbite.menu.dto.MenuItemResponse;
import com.quickbite.menu.service.MenuService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MenuController.class)
public class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MenuService menuService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testAddItem() throws Exception {
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Pasta");
        request.setPrice(300.0);
        request.setIsVegetarian(true);
        request.setCategoryId(2L);

        MenuItemResponse response = MenuItemResponse.builder()
                .id(1L)
                .name("Pasta")
                .price(300.0)
                .isVegetarian(true)
                .categoryId(2L)
                .build();

        when(menuService.addItem(any(MenuItemRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/menus/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Pasta"))
                .andExpect(jsonPath("$.price").value(300.0));
    }

    @Test
    public void testToggleAvailability() throws Exception {
        Long itemId = 1L;
        MenuItemResponse response = MenuItemResponse.builder()
                .id(itemId)
                .name("Pasta")
                .isAvailable(false)
                .build();

        when(menuService.toggleAvailability(any(), anyBoolean())).thenReturn(response);

        mockMvc.perform(put("/api/menus/items/{id}/availability", itemId)
                .param("isAvailable", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAvailable").value(false));
    }
}
