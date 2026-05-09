package com.speedster.investment.smart_investment_platform.notification.domain;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository {
    Notification save(Notification notification);
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Notification> findByUserIdAndReadFalse(UUID userId);
    void markAllAsRead(UUID userId);
    long countByUserIdAndReadFalse(UUID userId);
}
