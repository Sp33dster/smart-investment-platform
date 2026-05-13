package com.speedster.investment.smart_investment_platform.notification.application;

import com.speedster.investment.smart_investment_platform.notification.application.dto.NotificationResponse;
import com.speedster.investment.smart_investment_platform.notification.domain.Notification;
import com.speedster.investment.smart_investment_platform.notification.domain.NotificationRepository;
import com.speedster.investment.smart_investment_platform.notification.domain.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private UUID userId;
    private Notification unreadNotification;
    private Notification readNotification;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        unreadNotification = Notification.builder()
                .userId(userId)
                .assetId(UUID.randomUUID())
                .title("▲ Gold 10g change value by 3.00%")
                .message("Asset changed value")
                .type(NotificationType.PRICE_INCREASE)
                .read(false)
                .createdAt(Instant.now())
                .build();
        ReflectionTestUtils.setField(
                unreadNotification, "id", UUID.randomUUID());

        readNotification = Notification.builder()
                .userId(userId)
                .assetId(UUID.randomUUID())
                .title("▼ CD Projekt change value by 2.00%")
                .message("Asset changed value")
                .type(NotificationType.PRICE_DECREASE)
                .read(true)
                .createdAt(Instant.now().minusSeconds(3600))
                .build();
        ReflectionTestUtils.setField(
                readNotification, "id", UUID.randomUUID());
    }

    @Test
    @DisplayName("Should return all notifications for user")
    void shouldReturnAllNotificationsForUser() {
        // given
        given(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .willReturn(List.of(unreadNotification, readNotification));

        // when
        List<NotificationResponse> result =
                notificationService.getUserNotifications(userId);

        // then
        assertThat(result).hasSize(2);
        then(notificationRepository).should()
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    @DisplayName("Should return only unread notifications")
    void shouldReturnOnlyUnreadNotifications() {
        // given
        given(notificationRepository.findByUserIdAndReadFalse(userId))
                .willReturn(List.of(unreadNotification));

        // when
        List<NotificationResponse> result =
                notificationService.getUnreadNotifications(userId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).read()).isFalse();
    }

    @Test
    @DisplayName("Should return correct unread count")
    void shouldReturnCorrectUnreadCount() {
        // given
        given(notificationRepository.countByUserIdAndReadFalse(userId))
                .willReturn(3L);

        // when
        long count = notificationService.getUnreadCount(userId);

        // then
        assertThat(count).isEqualTo(3L);
    }

    @Test
    @DisplayName("Should return zero unread count when no unread notifications")
    void shouldReturnZeroUnreadCount() {
        // given
        given(notificationRepository.countByUserIdAndReadFalse(userId))
                .willReturn(0L);

        // when
        long count = notificationService.getUnreadCount(userId);

        // then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Should mark all notifications as read")
    void shouldMarkAllNotificationsAsRead() {
        // when
        notificationService.markAllAsRead(userId);

        // then
        then(notificationRepository).should().markAllAsRead(userId);
    }

    @Test
    @DisplayName("Should map notification to response correctly")
    void shouldMapNotificationToResponseCorrectly() {
        // given
        given(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .willReturn(List.of(unreadNotification));

        // when
        List<NotificationResponse> result =
                notificationService.getUserNotifications(userId);

        // then
        NotificationResponse response = result.get(0);
        assertThat(response.title())
                .isEqualTo("▲ Gold 10g change value by 3.00%");
        assertThat(response.type())
                .isEqualTo(NotificationType.PRICE_INCREASE);
        assertThat(response.read()).isFalse();
    }
}
