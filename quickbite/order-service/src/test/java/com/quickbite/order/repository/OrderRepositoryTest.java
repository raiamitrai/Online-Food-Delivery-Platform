package com.quickbite.order.repository;

import com.quickbite.order.entity.Order;
import com.quickbite.order.entity.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void testSaveAndFindByCustomerId() {
        // Arrange
        Order order1 = Order.builder()
                .customerId(101L)
                .restaurantId(201L)
                .status(OrderStatus.PLACED)
                .totalAmount(500.0)
                .createdAt(LocalDateTime.now())
                .build();
                
        Order order2 = Order.builder()
                .customerId(101L)
                .restaurantId(202L)
                .status(OrderStatus.DELIVERED)
                .totalAmount(300.0)
                .createdAt(LocalDateTime.now())
                .build();

        orderRepository.save(order1);
        orderRepository.save(order2);

        // Act
        List<Order> orders = orderRepository.findByCustomerId(101L);

        // Assert
        assertThat(orders).hasSize(2);
        assertThat(orders).extracting(Order::getRestaurantId).containsExactlyInAnyOrder(201L, 202L);
    }

    @Test
    public void testFindByRestaurantId() {
        Order order = Order.builder()
                .customerId(105L)
                .restaurantId(205L)
                .status(OrderStatus.PREPARING)
                .totalAmount(750.0)
                .createdAt(LocalDateTime.now())
                .build();
        orderRepository.save(order);

        List<Order> orders = orderRepository.findByRestaurantId(205L);

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getCustomerId()).isEqualTo(105L);
    }
}
