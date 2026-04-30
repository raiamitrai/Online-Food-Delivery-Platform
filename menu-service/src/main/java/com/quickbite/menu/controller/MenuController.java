package com.quickbite.menu.controller;

import com.quickbite.menu.dto.CategoryRequest;
import com.quickbite.menu.dto.CategoryResponse;
import com.quickbite.menu.dto.MenuItemRequest;
import com.quickbite.menu.dto.MenuItemResponse;
import com.quickbite.menu.service.MenuService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> addCategory(@RequestBody CategoryRequest request) {
        return ResponseEntity.ok(menuService.addCategory(request));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<CategoryResponse>> getMenuByRestaurantId(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuService.getMenuByRestaurantId(restaurantId));
    }

    @PostMapping("/items")
    public ResponseEntity<MenuItemResponse> addItem(@RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(menuService.addItem(request));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<MenuItemResponse> updateItem(@PathVariable Long id, @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(menuService.updateItem(id, request));
    }

    @PutMapping("/items/{id}/availability")
    public ResponseEntity<MenuItemResponse> toggleAvailability(@PathVariable Long id, @RequestParam boolean isAvailable) {
        return ResponseEntity.ok(menuService.toggleAvailability(id, isAvailable));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        menuService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        menuService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
