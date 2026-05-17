package com.quickbite.restaurant.consumer;

import com.quickbite.restaurant.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SagaConsumer {

    private final RabbitTemplate rabbitTemplate;

    public SagaConsumer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.RESTAURANT_QUEUE)
    public void processRestaurantOrder(Map<String, Object> eventData) {
        System.out.println(">>> Received PaymentSuccessfulEvent in Restaurant Service: " + eventData);
        
        try {
            Long orderId = Long.valueOf(eventData.get("orderId").toString());
            System.out.println(">>> Restaurant auto-accepting Order #" + orderId);
            
            // Simulate processing time
            Thread.sleep(500);
            
            Map<String, Object> acceptanceResult = new HashMap<>(eventData);
            acceptanceResult.put("restaurantStatus", "ACCEPTED");
            
            System.out.println(">>> Publishing RestaurantAcceptedEvent...");
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.SAGA_EXCHANGE,
                    "restaurant.accepted",
                    acceptanceResult
            );
            
        } catch (Exception e) {
            System.err.println(">>> Error processing restaurant order: " + e.getMessage());
            throw new RuntimeException("Restaurant processing failed", e);
        }
    }
}
