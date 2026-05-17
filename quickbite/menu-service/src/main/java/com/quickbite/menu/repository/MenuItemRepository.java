package com.quickbite.menu.repository;

import com.quickbite.menu.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByCategory_RestaurantId(Long restaurantId);
    List<MenuItem> findByCategory_RestaurantIdAndIsVegetarian(Long restaurantId, Boolean isVegetarian);
}
