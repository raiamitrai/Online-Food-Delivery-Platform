package com.quickbite.menu.service;

import com.quickbite.menu.dto.CategoryRequest;
import com.quickbite.menu.dto.CategoryResponse;
import com.quickbite.menu.dto.MenuItemRequest;
import com.quickbite.menu.dto.MenuItemResponse;

import java.util.List;

public interface MenuService {
    CategoryResponse addCategory(CategoryRequest request);
    List<CategoryResponse> getMenuByRestaurantId(Long restaurantId);
    
    MenuItemResponse addItem(MenuItemRequest request);
    MenuItemResponse updateItem(Long itemId, MenuItemRequest request);
    MenuItemResponse toggleAvailability(Long itemId, boolean isAvailable);
    void deleteItem(Long itemId);
    
    void deleteCategory(Long categoryId);
}
