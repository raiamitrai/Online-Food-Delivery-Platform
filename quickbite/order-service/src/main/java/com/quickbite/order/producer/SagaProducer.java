package com.quickbite.order.producer;

import com.quickbite.order.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SagaProducer {

    private final RabbitTemplate rabbitTemplate;

    public SagaProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrderCreated(Map<String, Object> orderData) {
        System.out.println(">>> Publishing OrderCreatedEvent to RabbitMQ...");
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SAGA_EXCHANGE,
                "order.created",
                orderData
        );
        System.out.println(">>> OrderCreatedEvent Published.");
    }
}
