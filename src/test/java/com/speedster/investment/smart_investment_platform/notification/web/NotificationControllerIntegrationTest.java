package com.speedster.investment.smart_investment_platform.notification.web;

import com.speedster.investment.smart_investment_platform.asset.domain.Asset;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetRepository;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetType;
import com.speedster.investment.smart_investment_platform.asset.infrastructure.persistance.JpaAssetRepository;
import com.speedster.investment.smart_investment_platform.notification.domain.Notification;
import com.speedster.investment.smart_investment_platform.notification.domain.NotificationRepository;
import com.speedster.investment.smart_investment_platform.notification.domain.NotificationType;
import com.speedster.investment.smart_investment_platform.notification.infrastructure.persistance.JpaNotificationRepository;
import com.speedster.investment.smart_investment_platform.shared.AbstractIntegrationTest;
import com.speedster.investment.smart_investment_platform.user.domain.Role;
import com.speedster.investment.smart_investment_platform.user.domain.User;
import com.speedster.investment.smart_investment_platform.user.domain.UserRepository;
import com.speedster.investment.smart_investment_platform.user.infrastructure.persistance.JpaUserRepository;
import com.speedster.investment.smart_investment_platform.user.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
class NotificationControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User testUser;
    private String jwtToken;
    private Asset goldAsset;

    @BeforeEach
    void setUp() {
        ((JpaNotificationRepository) notificationRepository).deleteAll();
        ((JpaAssetRepository) assetRepository).deleteAll();
        ((JpaUserRepository) userRepository).deleteAll();

        testUser = User.builder()
                .email("jan@example.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Jan")
                .lastName("Kowalski")
                .role(Role.USER)
                .build();
        userRepository.save(testUser);
        jwtToken = jwtService.generateToken(testUser);

        goldAsset = Asset.builder()
                .user(testUser)
                .name("Złoto 10g")
                .assetType(AssetType.GOLD)
                .quantity(new BigDecimal("10"))
                .purchasePrice(new BigDecimal("380.00"))
                .currency("PLN")
                .build();
        assetRepository.save(goldAsset);

    }

    @Test
    @DisplayName("Should return empty notifications list")
    void shouldReturnEmptyNotificationsList() throws Exception {
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Should return notifications for current user")
    void shouldReturnNotificationsForCurrentUser() throws Exception {
        // given
        Notification notification = Notification.builder()
                .userId(testUser.getId())
                .assetId(goldAsset.getId())
                .title("▲ Gold has changed the value")
                .message("Asset changed value")
                .type(NotificationType.PRICE_INCREASE)
                .read(false)
                .createdAt(Instant.now())
                .build();
        notificationRepository.save(notification);

        // when & then
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("▲ Gold has changed the value"))
                .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    @DisplayName("Should return unread count")
    void shouldReturnUnreadCount() throws Exception {
        // given
        Notification notification = Notification.builder()
                .userId(testUser.getId())
                .assetId(goldAsset.getId())
                .title("▲ Test")
                .message("Test message")
                .type(NotificationType.PRICE_INCREASE)
                .read(false)
                .createdAt(Instant.now())
                .build();
        notificationRepository.save(notification);

        // when & then
        mockMvc.perform(get("/api/v1/notifications/unread/count")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(1));
    }

    @Test
    @DisplayName("Should mark all notifications as read")
    void shouldMarkAllNotificationsAsRead() throws Exception {
        // given
        Notification notification = Notification.builder()
                .userId(testUser.getId())
                .assetId(goldAsset.getId())
                .title("▲ Test")
                .message("Test message")
                .type(NotificationType.PRICE_INCREASE)
                .read(false)
                .createdAt(Instant.now())
                .build();
        notificationRepository.save(notification);

        // when
        mockMvc.perform(put("/api/v1/notifications/read-all")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        // then
        mockMvc.perform(get("/api/v1/notifications/unread/count")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(0));
    }

    @Test
    @DisplayName("Should return 403 when no token")
    void shouldReturn403WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isForbidden());
    }
}
