package com.quickbite.order.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String NOTIFICATION_QUEUE = "notificationQueue";
    public static final String NOTIFICATION_EXCHANGE = "notificationExchange";
    public static final String NOTIFICATION_ROUTING_KEY = "notificationRoutingKey";

    public static final String SAGA_EXCHANGE = "sagaExchange";
    
    // DLQ definitions
    public static final String DEAD_LETTER_EXCHANGE = "sagaDeadLetterExchange";
    public static final String ORDER_DLQ = "orderSagaDLQ";

    // Main queue
    public static final String ORDER_SAGA_QUEUE = "orderSagaQueue";
    
    // Routing keys we listen to
    public static final String PAYMENT_SUCCESSFUL_ROUTING_KEY = "payment.successful";
    public static final String PAYMENT_FAILED_ROUTING_KEY = "payment.failed";
    public static final String RESTAURANT_ACCEPTED_ROUTING_KEY = "restaurant.accepted";
    public static final String RESTAURANT_REJECTED_ROUTING_KEY = "restaurant.rejected";

    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with(NOTIFICATION_ROUTING_KEY);
    }

    // Saga Beans
    @Bean
    public TopicExchange sagaExchange() {
        return new TopicExchange(SAGA_EXCHANGE);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Queue orderDLQ() {
        return QueueBuilder.durable(ORDER_DLQ).build();
    }

    @Bean
    public Binding orderDlqBinding() {
        return BindingBuilder.bind(orderDLQ()).to(deadLetterExchange()).with(ORDER_SAGA_QUEUE);
    }

    @Bean
    public Queue orderSagaQueue() {
        java.util.Map<String, Object> args = new java.util.HashMap<>();
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        args.put("x-dead-letter-routing-key", ORDER_SAGA_QUEUE);
        return QueueBuilder.durable(ORDER_SAGA_QUEUE).withArguments(args).build();
    }

    @Bean
    public Binding paymentSuccessfulBinding() {
        return BindingBuilder.bind(orderSagaQueue()).to(sagaExchange()).with(PAYMENT_SUCCESSFUL_ROUTING_KEY);
    }

    @Bean
    public Binding paymentFailedBinding() {
        return BindingBuilder.bind(orderSagaQueue()).to(sagaExchange()).with(PAYMENT_FAILED_ROUTING_KEY);
    }

    @Bean
    public Binding restaurantAcceptedBinding() {
        return BindingBuilder.bind(orderSagaQueue()).to(sagaExchange()).with(RESTAURANT_ACCEPTED_ROUTING_KEY);
    }

    @Bean
    public Binding restaurantRejectedBinding() {
        return BindingBuilder.bind(orderSagaQueue()).to(sagaExchange()).with(RESTAURANT_REJECTED_ROUTING_KEY);
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
