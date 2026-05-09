package com.speedster.investment.smart_investment_platform.notification.application;

import com.speedster.investment.smart_investment_platform.notification.domain.Notification;
import com.speedster.investment.smart_investment_platform.notification.domain.NotificationRepository;
import com.speedster.investment.smart_investment_platform.notification.domain.NotificationType;
import com.speedster.investment.smart_investment_platform.shared.event.AssetValueChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final NotificationRepository notificationRepository;

    private static final BigDecimal CHANGE_THRESHOLD = new BigDecimal("0.1");

    @EventListener
    @Async
    public void onAssetValueChanged(AssetValueChangedEvent event){
        log.info("=== EVENT RECEIVED: {} ===", event.assetName());
        BigDecimal absChange = event.changePercent().abs();

        if (absChange.compareTo(CHANGE_THRESHOLD) < 0){
            return;
        }

        NotificationType type = determineType(event.changePercent());
        String title = buildTitle(event, type);
        String message = buildMessage(event);

        Notification notification = Notification.builder()
                .userId(event.userId())
                .assetId(event.assetId())
                .title(title)
                .message(message)
                .type(type)
                .read(false)
                .createdAt(Instant.now())
                .build();

        notificationRepository.save(notification);
        log.info("Notification created for user {} - {}", event.userId(), title);
    }

    private NotificationType determineType(BigDecimal changePercent){
        if (changePercent.abs().compareTo(new BigDecimal("10.0")) >= 0){
            return NotificationType.SIGNIFICANT_CHANGE;
        }
        return changePercent.compareTo(BigDecimal.ZERO) > 0
                ? NotificationType.PRICE_INCREASE
                : NotificationType.PRICE_DECREASE;
    }

    private String buildTitle(AssetValueChangedEvent event, NotificationType notificationType){
        String direction = event.changePercent().compareTo(BigDecimal.ZERO) > 0 ? "▲" : "▼";
        return String.format("%s %s change value by %s%%",
                direction, event.assetName(),
                event.changePercent().abs().setScale(2, RoundingMode.HALF_UP));
    }

    private String buildMessage(AssetValueChangedEvent event){
        return String.format(
                "Asset '%s' change value from %.2f PLN to %.2f PLN (%+.2f%%)",
                event.assetName(),
                event.previousValue(),
                event.currentValue(),
                event.changePercent()
                );
    }

}
