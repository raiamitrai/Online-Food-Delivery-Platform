package com.quickbite.delivery.repository;

import com.quickbite.delivery.entity.DeliveryAgent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class DeliveryAgentRepositoryTest {

    @Autowired
    private DeliveryAgentRepository deliveryAgentRepository;

    @Test
    public void testFindByIsAvailableTrue() {
        // Arrange
        DeliveryAgent availableAgent = DeliveryAgent.builder()
                .userId(1L)
                .name("John Doe")
                .phone("1234567890")
                .isAvailable(true)
                .build();
                
        DeliveryAgent busyAgent = DeliveryAgent.builder()
                .userId(2L)
                .name("Jane Doe")
                .phone("0987654321")
                .isAvailable(false)
                .currentOrderId(101L)
                .build();

        deliveryAgentRepository.save(availableAgent);
        deliveryAgentRepository.save(busyAgent);

        // Act
        List<DeliveryAgent> availableAgents = deliveryAgentRepository.findByIsAvailableTrue();

        // Assert
        assertThat(availableAgents).hasSize(1);
        assertThat(availableAgents.get(0).getName()).isEqualTo("John Doe");
    }

    @Test
    public void testFindByUserId() {
        // Arrange
        DeliveryAgent agent = DeliveryAgent.builder()
                .userId(5L)
                .name("Mark")
                .phone("5555555555")
                .isAvailable(true)
                .build();
        deliveryAgentRepository.save(agent);

        // Act
        Optional<DeliveryAgent> foundAgent = deliveryAgentRepository.findByUserId(5L);

        // Assert
        assertThat(foundAgent).isPresent();
        assertThat(foundAgent.get().getName()).isEqualTo("Mark");
    }
}
