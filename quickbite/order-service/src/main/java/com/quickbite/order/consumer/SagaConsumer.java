package com.quickbite.order.consumer;

import com.quickbite.order.config.RabbitMQConfig;
import com.quickbite.order.entity.Order;
import com.quickbite.order.entity.OrderStatus;
import com.quickbite.order.repository.OrderRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class SagaConsumer {

    private final OrderRepository orderRepository;

    public SagaConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_SAGA_QUEUE)
    public void processSagaEvents(Map<String, Object> eventData) {
        System.out.println(">>> Received Saga Event in Order Service: " + eventData);
        
        Long orderId = Long.valueOf(eventData.get("orderId").toString());
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        
        if (orderOpt.isEmpty()) {
            System.err.println(">>> Order not found for ID: " + orderId);
            return;
        }

        Order order = orderOpt.get();

        if (eventData.containsKey("paymentStatus")) {
            String paymentStatus = eventData.get("paymentStatus").toString();
            if ("SUCCESS".equals(paymentStatus)) {
                System.out.println(">>> Payment Success for Order #" + orderId + ". Waiting for Restaurant to Accept...");
                // Note: The restaurant-service is also listening to payment.successful,
                // so we don't necessarily need to change the status here, or we can set it to PAID.
                // For now, we wait for restaurant acceptance to move to CONFIRMED.
            } else {
                System.out.println(">>> Payment Failed for Order #" + orderId + ". Cancelling Order.");
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
            }
        } else if (eventData.containsKey("restaurantStatus")) {
            String restaurantStatus = eventData.get("restaurantStatus").toString();
            if ("ACCEPTED".equals(restaurantStatus)) {
                System.out.println(">>> Restaurant Accepted Order #" + orderId + ". Status -> CONFIRMED.");
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);
            } else {
                System.out.println(">>> Restaurant Rejected Order #" + orderId + ". Status -> CANCELLED.");
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
            }
        }
    }
}
