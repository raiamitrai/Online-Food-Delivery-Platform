package com.quickbite.menu.service;

import com.quickbite.menu.dto.MenuItemRequest;
import com.quickbite.menu.dto.MenuItemResponse;
import com.quickbite.menu.entity.MenuCategory;
import com.quickbite.menu.entity.MenuItem;
import com.quickbite.menu.repository.MenuCategoryRepository;
import com.quickbite.menu.repository.MenuItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MenuServiceImplTest {

    @Mock
    private MenuCategoryRepository categoryRepository;

    @Mock
    private MenuItemRepository itemRepository;

    @InjectMocks
    private MenuServiceImpl menuService;

    @Test
    public void testAddItem_Success() {
        // Arrange
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Burger");
        request.setDescription("Veg Burger");
        request.setPrice(150.0);
        request.setIsVegetarian(true);
        request.setCategoryId(1L);

        MenuCategory category = MenuCategory.builder().id(1L).name("Fast Food").restaurantId(101L).build();

        MenuItem savedItem = MenuItem.builder()
                .id(10L)
                .name("Burger")
                .price(150.0)
                .isVegetarian(true)
                .isAvailable(true)
                .category(category)
                .build();

        when(categoryRepository.findById(any())).thenReturn(Optional.of(category));
        when(itemRepository.save(any())).thenReturn(savedItem);

        // Act
        MenuItemResponse response = menuService.addItem(request);

        // Assert
        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Burger", response.getName());
        assertEquals(1L, response.getCategoryId());
    }

    @Test
    public void testToggleAvailability_Success() {
        // Arrange
        MenuCategory category = MenuCategory.builder().id(1L).build();
        MenuItem item = MenuItem.builder()
                .id(10L)
                .name("Pizza")
                .isAvailable(true)
                .category(category)
                .build();

        when(itemRepository.findById(any())).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        MenuItemResponse response = menuService.toggleAvailability(10L, false);

        // Assert
        assertFalse(response.getIsAvailable());
        verify(itemRepository, times(1)).save(any(MenuItem.class));
    }

    @Test
    public void testDeleteItem_Success() {
        // Arrange
        Long itemId = 10L;
        doNothing().when(itemRepository).deleteById(any());

        // Act
        menuService.deleteItem(itemId);

        // Assert
        verify(itemRepository, times(1)).deleteById(itemId);
    }
}
