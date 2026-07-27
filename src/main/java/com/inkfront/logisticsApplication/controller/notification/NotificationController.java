package com.inkfront.logisticsApplication.controller.notification;

import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.common.NotificationDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.service.interfaces.NotificationService;
import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "Notification management endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get user notifications")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<NotificationDTO>>> getNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String userId = authentication.getName();
        log.info("Get notifications request for user: {}", userId);
        PaginatedResponseDTO<NotificationDTO> response = notificationService.getUserNotifications(userId, page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications")
    public ResponseEntity<ApiResponseDTO<List<NotificationDTO>>> getUnreadNotifications(
            Authentication authentication) {
        String userId = authentication.getName();
        log.info("Get unread notifications request for user: {}", userId);
        List<NotificationDTO> response = notificationService.getUserUnreadNotifications(userId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/{notificationId}")
    @Operation(summary = "Get notification by ID")
    public ResponseEntity<ApiResponseDTO<NotificationDTO>> getNotificationById(@PathVariable String notificationId) {
        log.info("Get notification by ID request for: {}", notificationId);
        NotificationDTO response = notificationService.getNotificationById(notificationId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<ApiResponseDTO<Void>> markAsRead(@PathVariable String notificationId) {
        log.info("Mark notification as read request for: {}", notificationId);
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(ApiResponseDTO.success("Notification marked as read", null));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponseDTO<Void>> markAllAsRead(Authentication authentication) {
        String userId = authentication.getName();
        log.info("Mark all notifications as read request for user: {}", userId);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponseDTO.success("All notifications marked as read", null));
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete notification")
    public ResponseEntity<ApiResponseDTO<Void>> deleteNotification(@PathVariable String notificationId) {
        log.info("Delete notification request for: {}", notificationId);
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(ApiResponseDTO.success("Notification deleted successfully", null));
    }

    @DeleteMapping("/delete-all")
    @Operation(summary = "Delete all user notifications")
    public ResponseEntity<ApiResponseDTO<Void>> deleteAllNotifications(Authentication authentication) {
        String userId = authentication.getName();
        log.info("Delete all notifications request for user: {}", userId);
        notificationService.deleteAllUserNotifications(userId);
        return ResponseEntity.ok(ApiResponseDTO.success("All notifications deleted successfully", null));
    }

    @GetMapping("/count/unread")
    @Operation(summary = "Get unread notifications count")
    public ResponseEntity<ApiResponseDTO<Long>> getUnreadCount(Authentication authentication) {
        String userId = authentication.getName();
        log.info("Get unread notifications count request for user: {}", userId);
        long count = notificationService.countUnreadNotifications(userId);
        return ResponseEntity.ok(ApiResponseDTO.success("Unread count retrieved", count));
    }
}