package com.quickbite.delivery.repository;

import com.quickbite.delivery.entity.DeliveryAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryAgentRepository extends JpaRepository<DeliveryAgent, Long> {
    List<DeliveryAgent> findByIsAvailableTrue();
    java.util.Optional<DeliveryAgent> findByUserId(Long userId);
}
