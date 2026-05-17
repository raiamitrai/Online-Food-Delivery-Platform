package com.quickbite.payment.consumer;

import com.quickbite.payment.config.RabbitMQConfig;
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

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_QUEUE)
    public void processPayment(Map<String, Object> orderData) {
        System.out.println(">>> Received OrderCreatedEvent in Payment Service: " + orderData);
        
        try {
            // Simulate Payment Processing
            Long orderId = Long.valueOf(orderData.get("orderId").toString());
            Double amount = Double.valueOf(orderData.get("totalAmount").toString());
            System.out.println(">>> Processing payment for Order #" + orderId + ", Amount: ₹" + amount);
            
            // Sleep for 1 second to simulate processing
            Thread.sleep(1000);
            
            // Assuming 100% success rate for simulation
            boolean paymentSuccess = true;
            
            Map<String, Object> paymentResult = new HashMap<>(orderData);
            
            if (paymentSuccess) {
                paymentResult.put("paymentStatus", "SUCCESS");
                paymentResult.put("transactionId", "TXN" + System.currentTimeMillis());
                
                System.out.println(">>> Payment Successful! Publishing PaymentSuccessfulEvent...");
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.SAGA_EXCHANGE,
                        "payment.successful",
                        paymentResult
                );
            } else {
                paymentResult.put("paymentStatus", "FAILED");
                System.out.println(">>> Payment Failed! Publishing PaymentFailedEvent...");
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.SAGA_EXCHANGE,
                        "payment.failed",
                        paymentResult
                );
            }
            
        } catch (Exception e) {
            System.err.println(">>> Error processing payment: " + e.getMessage());
            // In a real scenario, this might trigger a retry or go to DLQ.
            // Spring AMQP will automatically NACK and retry/DLQ if exception is thrown.
            throw new RuntimeException("Payment processing failed", e);
        }
    }
}
