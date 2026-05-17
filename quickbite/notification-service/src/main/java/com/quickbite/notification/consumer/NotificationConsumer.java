package com.quickbite.notification.consumer;

import com.quickbite.notification.dto.NotificationRequest;
import com.quickbite.notification.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private final NotificationService notificationService;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "notificationQueue")
    public void consumeNotification(NotificationRequest notificationRequest) {
        System.out.println("<<< Received Notification Request from RabbitMQ: " + notificationRequest);
        try {
            notificationService.sendNotification(notificationRequest);
            System.out.println("<<< Notification Processed Successfully.");
        } catch (Exception e) {
            System.err.println("<<< Error Processing Notification: " + e.getMessage());
        }
    }
}
