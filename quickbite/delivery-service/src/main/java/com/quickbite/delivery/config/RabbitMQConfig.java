package com.quickbite.delivery.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String SAGA_EXCHANGE = "sagaExchange";
    
    // DLQ definitions
    public static final String DEAD_LETTER_EXCHANGE = "sagaDeadLetterExchange";
    public static final String DELIVERY_DLQ = "deliverySagaDLQ";

    // Main queue
    public static final String DELIVERY_QUEUE = "deliverySagaQueue";
    
    // Routing keys
    public static final String RESTAURANT_ACCEPTED_ROUTING_KEY = "restaurant.accepted";

    @Bean
    public TopicExchange sagaExchange() {
        return new TopicExchange(SAGA_EXCHANGE);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Queue deliveryDLQ() {
        return QueueBuilder.durable(DELIVERY_DLQ).build();
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(deliveryDLQ()).to(deadLetterExchange()).with(DELIVERY_QUEUE);
    }

    @Bean
    public Queue deliveryQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        args.put("x-dead-letter-routing-key", DELIVERY_QUEUE);
        return QueueBuilder.durable(DELIVERY_QUEUE).withArguments(args).build();
    }

    @Bean
    public Binding deliveryBinding() {
        return BindingBuilder.bind(deliveryQueue()).to(sagaExchange()).with(RESTAURANT_ACCEPTED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
