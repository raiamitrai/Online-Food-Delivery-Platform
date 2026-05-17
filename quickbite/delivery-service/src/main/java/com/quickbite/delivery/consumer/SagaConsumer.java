package com.quickbite.delivery.consumer;

import com.quickbite.delivery.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SagaConsumer {

    @RabbitListener(queues = RabbitMQConfig.DELIVERY_QUEUE)
    public void processDeliveryAssignment(Map<String, Object> eventData) {
        System.out.println(">>> Received RestaurantAcceptedEvent in Delivery Service: " + eventData);
        
        try {
            Long orderId = Long.valueOf(eventData.get("orderId").toString());
            System.out.println(">>> Assigning Delivery Agent for Order #" + orderId);
            
            // Simulate agent assignment
            Thread.sleep(800);
            
            // In a real scenario, this would update the delivery agent in the DB
            // and optionally call back to the order-service (though order-service already
            // listens to restaurant.accepted and updates its status).
            
            System.out.println(">>> Delivery Agent Assigned Successfully for Order #" + orderId);
            
        } catch (Exception e) {
            System.err.println(">>> Error assigning delivery agent: " + e.getMessage());
            throw new RuntimeException("Delivery assignment failed", e);
        }
    }
}
