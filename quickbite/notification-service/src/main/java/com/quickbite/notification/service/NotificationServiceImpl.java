package com.quickbite.notification.service;

import com.quickbite.notification.dto.NotificationRequest;
import com.quickbite.notification.dto.NotificationResponse;
import com.quickbite.notification.entity.Notification;
import com.quickbite.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public NotificationResponse sendNotification(NotificationRequest request) {
        Notification notification = Notification.builder()
                .customerId(request.getCustomerId())
                .type(request.getType())
                .message(request.getMessage())
                .isSent(true) // Simulating successful send
                .createdAt(LocalDateTime.now())
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        // Simulate sending email/sms
        System.out.println(">>> SIMULATION: Sending " + request.getType() + " to Customer ID " 
                + request.getCustomerId() + " : " + request.getMessage());

        return mapToResponse(savedNotification);
    }

    @Override
    public List<NotificationResponse> getNotificationHistory(Long customerId) {
        return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .customerId(notification.getCustomerId())
                .type(notification.getType())
                .message(notification.getMessage())
                .isSent(notification.getIsSent())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
