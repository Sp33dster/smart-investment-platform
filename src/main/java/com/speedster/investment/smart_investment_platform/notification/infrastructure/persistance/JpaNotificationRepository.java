package com.speedster.investment.smart_investment_platform.notification.infrastructure.persistance;

import com.speedster.investment.smart_investment_platform.notification.domain.Notification;
import com.speedster.investment.smart_investment_platform.notification.domain.NotificationRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaNotificationRepository extends JpaRepository<Notification, UUID>, NotificationRepository {

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.userId = :userId")
    void markAllAsRead(UUID userId);
}
