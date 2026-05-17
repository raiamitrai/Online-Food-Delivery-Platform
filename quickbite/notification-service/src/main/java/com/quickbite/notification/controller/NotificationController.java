package com.quickbite.notification.controller;

import com.quickbite.notification.dto.NotificationRequest;
import com.quickbite.notification.dto.NotificationResponse;
import com.quickbite.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> sendNotification(@RequestBody NotificationRequest request) {
        return ResponseEntity.ok(notificationService.sendNotification(request));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationHistory(@PathVariable Long customerId) {
        return ResponseEntity.ok(notificationService.getNotificationHistory(customerId));
    }
}
