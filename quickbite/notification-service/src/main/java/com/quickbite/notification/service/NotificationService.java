package com.quickbite.notification.service;

import com.quickbite.notification.dto.NotificationRequest;
import com.quickbite.notification.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {
    NotificationResponse sendNotification(NotificationRequest request);
    List<NotificationResponse> getNotificationHistory(Long customerId);
}
