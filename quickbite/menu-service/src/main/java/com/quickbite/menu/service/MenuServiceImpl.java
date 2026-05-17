package com.quickbite.menu.service;

import com.quickbite.menu.dto.CategoryRequest;
import com.quickbite.menu.dto.CategoryResponse;
import com.quickbite.menu.dto.MenuItemRequest;
import com.quickbite.menu.dto.MenuItemResponse;
import com.quickbite.menu.entity.MenuCategory;
import com.quickbite.menu.entity.MenuItem;
import com.quickbite.menu.repository.MenuCategoryRepository;
import com.quickbite.menu.repository.MenuItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

@Service
public class MenuServiceImpl implements MenuService {

    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;

    public MenuServiceImpl(MenuCategoryRepository categoryRepository, MenuItemRepository itemRepository) {
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    @CacheEvict(value = "menu", allEntries = true)
    public CategoryResponse addCategory(CategoryRequest request) {
        MenuCategory category = MenuCategory.builder()
                .name(request.getName())
                .restaurantId(request.getRestaurantId())
                .build();
        MenuCategory savedCategory = categoryRepository.save(category);
        return mapToCategoryResponse(savedCategory);
    }

    @Override
    @Cacheable(value = "menu", key = "#restaurantId")
    public List<CategoryResponse> getMenuByRestaurantId(Long restaurantId) {
        List<MenuCategory> categories = categoryRepository.findByRestaurantId(restaurantId);
        return categories.stream().map(this::mapToCategoryResponse).collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "menu", allEntries = true)
    public MenuItemResponse addItem(MenuItemRequest request) {
        MenuCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        MenuItem item = MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .isVegetarian(request.getIsVegetarian())
                .isAvailable(true)
                .category(category)
                .build();

        MenuItem savedItem = itemRepository.save(item);
        return mapToItemResponse(savedItem);
    }

    @Override
    @CacheEvict(value = "menu", allEntries = true)
    public MenuItemResponse updateItem(Long itemId, MenuItemRequest request) {
        MenuItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setIsVegetarian(request.getIsVegetarian());
        
        if (request.getCategoryId() != null && !item.getCategory().getId().equals(request.getCategoryId())) {
            MenuCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            item.setCategory(category);
        }
        
        MenuItem updatedItem = itemRepository.save(item);
        return mapToItemResponse(updatedItem);
    }

    @Override
    @CacheEvict(value = "menu", allEntries = true)
    public MenuItemResponse toggleAvailability(Long itemId, boolean isAvailable) {
        MenuItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        item.setIsAvailable(isAvailable);
        MenuItem updatedItem = itemRepository.save(item);
        return mapToItemResponse(updatedItem);
    }

    @Override
    @CacheEvict(value = "menu", allEntries = true)
    public void deleteItem(Long itemId) {
        itemRepository.deleteById(itemId);
    }

    @Override
    @CacheEvict(value = "menu", allEntries = true)
    public void deleteCategory(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }

    private CategoryResponse mapToCategoryResponse(MenuCategory category) {
        List<MenuItemResponse> items = category.getItems() != null ? 
                category.getItems().stream().map(this::mapToItemResponse).collect(Collectors.toList()) 
                : List.of();

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .restaurantId(category.getRestaurantId())
                .items(items)
                .build();
    }

    private MenuItemResponse mapToItemResponse(MenuItem item) {
        return MenuItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .isVegetarian(item.getIsVegetarian())
                .isAvailable(item.getIsAvailable())
                .categoryId(item.getCategory().getId())
                .build();
    }
}
