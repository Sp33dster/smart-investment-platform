package com.speedster.investment.smart_investment_platform.notification.web;

import com.speedster.investment.smart_investment_platform.notification.application.NotificationService;
import com.speedster.investment.smart_investment_platform.notification.application.dto.NotificationResponse;
import com.speedster.investment.smart_investment_platform.shared.exception.ResourceNotFoundException;
import com.speedster.investment.smart_investment_platform.user.domain.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    private UUID getCurrentUserId(Authentication authentication){
        return  userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", authentication.getName()))
                .getId();
    }

    @GetMapping
    @Operation(summary = "Get all notifications")
    public List<NotificationResponse> getNotifications(Authentication authentication){
        return notificationService.getUserNotifications(getCurrentUserId(authentication));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications")
    public List<NotificationResponse> getUnreadNotifications(Authentication authentication){
        return notificationService.getUnreadNotifications(getCurrentUserId(authentication));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Get unread notifications count")
    public Map<String, Long> getUnreadCount(Authentication authentication){
        long count = notificationService.getUnreadCount(getCurrentUserId(authentication));
        return Map.of("unreadCount", count);
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication){
        notificationService.markAllAsRead(getCurrentUserId(authentication));
        return ResponseEntity.noContent().build();
    }
}
