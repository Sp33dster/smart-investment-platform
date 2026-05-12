package com.speedster.investment.smart_investment_platform.notification.application;

import com.speedster.investment.smart_investment_platform.asset.domain.AssetType;
import com.speedster.investment.smart_investment_platform.notification.domain.Notification;
import com.speedster.investment.smart_investment_platform.notification.domain.NotificationRepository;
import com.speedster.investment.smart_investment_platform.notification.domain.NotificationType;
import com.speedster.investment.smart_investment_platform.shared.event.AssetValueChangedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class NotificationListenerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationListener notificationListener;

    private AssetValueChangedEvent buildEvent(BigDecimal changePercent) {
        return new AssetValueChangedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Złoto 10g",
                AssetType.GOLD,
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00").add(
                        new BigDecimal("1000.00")
                                .multiply(changePercent)
                                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)),
                changePercent,
                Instant.now()
        );
    }

    @Test
    @DisplayName("Should save notification when change exceeds threshold")
    void shouldSaveNotificationWhenChangeExceedsThreshold() {
        // given
        AssetValueChangedEvent event = buildEvent(new BigDecimal("5.0"));

        // when
        notificationListener.onAssetValueChanged(event);

        // then
        then(notificationRepository).should().save(any(Notification.class));
    }

    @Test
    @DisplayName("Should not save notification when change is below threshold")
    void shouldNotSaveNotificationWhenChangeBelowThreshold() {
        // given
        AssetValueChangedEvent event = buildEvent(new BigDecimal("0.05"));

        // when
        notificationListener.onAssetValueChanged(event);

        // then
        then(notificationRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("Should create PRICE_INCREASE notification for positive change")
    void shouldCreatePriceIncreaseNotification() {
        // given
        AssetValueChangedEvent event = buildEvent(new BigDecimal("3.0"));

        ArgumentCaptor<Notification> captor =
                ArgumentCaptor.forClass(Notification.class);

        // when
        notificationListener.onAssetValueChanged(event);

        // then
        then(notificationRepository).should().save(captor.capture());
        assertThat(captor.getValue().getType())
                .isEqualTo(NotificationType.PRICE_INCREASE);
    }

    @Test
    @DisplayName("Should create SIGNIFICANT_CHANGE notification for large change")
    void shouldCreateSignificantChangeNotification() {
        // given
        AssetValueChangedEvent event = buildEvent(new BigDecimal("15.0"));

        ArgumentCaptor<Notification> captor =
                ArgumentCaptor.forClass(Notification.class);

        // when
        notificationListener.onAssetValueChanged(event);

        // then
        then(notificationRepository).should().save(captor.capture());
        assertThat(captor.getValue().getType())
                .isEqualTo(NotificationType.SIGNIFICANT_CHANGE);
    }

    @Test
    @DisplayName("Should create PRICE_DECREASE notification for negative change")
    void shouldCreatePriceDecreaseNotification() {
        // given
        AssetValueChangedEvent event = buildEvent(new BigDecimal("-3.0"));

        ArgumentCaptor<Notification> captor =
                ArgumentCaptor.forClass(Notification.class);

        // when
        notificationListener.onAssetValueChanged(event);

        // then
        then(notificationRepository).should().save(captor.capture());
        assertThat(captor.getValue().getType())
                .isEqualTo(NotificationType.PRICE_DECREASE);
    }
}
