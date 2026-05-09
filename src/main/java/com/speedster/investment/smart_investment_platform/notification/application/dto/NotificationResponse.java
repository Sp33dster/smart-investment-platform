package com.speedster.investment.smart_investment_platform.notification.application.dto;

import com.speedster.investment.smart_investment_platform.notification.domain.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String title,
        String message,
        NotificationType type,
        boolean read,
        Instant createdAt
) {
}
