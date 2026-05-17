package com.quickbite.restaurant.config;

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
    public static final String RESTAURANT_DLQ = "restaurantSagaDLQ";

    // Main queue
    public static final String RESTAURANT_QUEUE = "restaurantSagaQueue";
    
    // Routing keys
    public static final String PAYMENT_SUCCESSFUL_ROUTING_KEY = "payment.successful";

    @Bean
    public TopicExchange sagaExchange() {
        return new TopicExchange(SAGA_EXCHANGE);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Queue restaurantDLQ() {
        return QueueBuilder.durable(RESTAURANT_DLQ).build();
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(restaurantDLQ()).to(deadLetterExchange()).with(RESTAURANT_QUEUE);
    }

    @Bean
    public Queue restaurantQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        args.put("x-dead-letter-routing-key", RESTAURANT_QUEUE);
        return QueueBuilder.durable(RESTAURANT_QUEUE).withArguments(args).build();
    }

    @Bean
    public Binding restaurantBinding() {
        return BindingBuilder.bind(restaurantQueue()).to(sagaExchange()).with(PAYMENT_SUCCESSFUL_ROUTING_KEY);
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
