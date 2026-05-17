package com.quickbite.cart.repository;

import com.quickbite.cart.entity.Cart;
import com.quickbite.cart.entity.CartItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Test
    public void testFindByCustomerId() {
        // Arrange
        Cart cart = Cart.builder()
                .customerId(901L)
                .restaurantId(101L)
                .build();
        
        CartItem item = CartItem.builder()
                .menuItemId(201L)
                .menuItemName("Burger")
                .price(150.0)
                .quantity(2)
                .build();
                
        cart.addItem(item);
        cartRepository.save(cart);

        // Act
        Optional<Cart> foundCart = cartRepository.findByCustomerId(901L);

        // Assert
        assertThat(foundCart).isPresent();
        assertThat(foundCart.get().getCustomerId()).isEqualTo(901L);
        assertThat(foundCart.get().getItems()).hasSize(1);
        assertThat(foundCart.get().getItems().get(0).getMenuItemName()).isEqualTo("Burger");
    }
}
