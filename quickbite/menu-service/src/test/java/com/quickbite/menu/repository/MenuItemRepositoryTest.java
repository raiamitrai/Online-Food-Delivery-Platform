package com.quickbite.menu.repository;

import com.quickbite.menu.entity.MenuCategory;
import com.quickbite.menu.entity.MenuItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class MenuItemRepositoryTest {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private MenuCategoryRepository categoryRepository;

    @Test
    public void testFindByCategory_RestaurantId() {
        // Arrange
        MenuCategory category = MenuCategory.builder()
                .name("Starters")
                .restaurantId(501L)
                .build();
        category = categoryRepository.save(category);

        MenuItem item1 = MenuItem.builder()
                .name("Paneer Tikka")
                .price(250.0)
                .isVegetarian(true)
                .isAvailable(true)
                .category(category)
                .build();
                
        MenuItem item2 = MenuItem.builder()
                .name("Chicken Tikka")
                .price(350.0)
                .isVegetarian(false)
                .isAvailable(true)
                .category(category)
                .build();

        menuItemRepository.save(item1);
        menuItemRepository.save(item2);

        // Act
        List<MenuItem> items = menuItemRepository.findByCategory_RestaurantId(501L);

        // Assert
        assertThat(items).hasSize(2);
    }

    @Test
    public void testFindByCategory_RestaurantIdAndIsVegetarian() {
        // Arrange
        MenuCategory category = MenuCategory.builder()
                .name("Mains")
                .restaurantId(502L)
                .build();
        category = categoryRepository.save(category);

        MenuItem vegItem = MenuItem.builder()
                .name("Dal Makhani")
                .price(200.0)
                .isVegetarian(true)
                .category(category)
                .build();

        MenuItem nonVegItem = MenuItem.builder()
                .name("Butter Chicken")
                .price(400.0)
                .isVegetarian(false)
                .category(category)
                .build();

        menuItemRepository.save(vegItem);
        menuItemRepository.save(nonVegItem);

        // Act
        List<MenuItem> vegItems = menuItemRepository.findByCategory_RestaurantIdAndIsVegetarian(502L, true);

        // Assert
        assertThat(vegItems).hasSize(1);
        assertThat(vegItems.get(0).getName()).isEqualTo("Dal Makhani");
    }
}
