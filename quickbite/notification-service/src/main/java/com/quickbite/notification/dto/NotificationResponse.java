package com.quickbite.notification.dto;

import com.quickbite.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private Long customerId;
    private NotificationType type;
    private String message;
    private Boolean isSent;
    private LocalDateTime createdAt;
}
