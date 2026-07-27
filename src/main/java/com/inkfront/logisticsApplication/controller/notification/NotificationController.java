package com.inkfront.logisticsApplication.controller.notification;

import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.common.NotificationDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.security.AuthenticatedUser;
import com.inkfront.logisticsApplication.service.interfaces.NotificationService;
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

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        log.info("Get notifications request for user: {}", user.getEmail());

        PaginatedResponseDTO<NotificationDTO> response =
                notificationService.getUserNotifications(user.getId(), page, size);

        return ResponseEntity.ok(
                ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response)
        );
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications")
    public ResponseEntity<ApiResponseDTO<List<NotificationDTO>>> getUnreadNotifications(
            Authentication authentication) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        log.info("Get unread notifications request for user: {}", user.getEmail());

        List<NotificationDTO> response =
                notificationService.getUserUnreadNotifications(user.getId());

        return ResponseEntity.ok(
                ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response)
        );
    }

    @GetMapping("/{notificationId}")
    @Operation(summary = "Get notification by ID")
    public ResponseEntity<ApiResponseDTO<NotificationDTO>> getNotificationById(
            Authentication authentication,
            @PathVariable String notificationId) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        log.info("Get notification by ID request for user: {} notification: {}",
                user.getEmail(), notificationId);

        NotificationDTO response =
                notificationService.getNotificationById(
                        user.getId(),
                        notificationId
                );

        return ResponseEntity.ok(
                ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response)
        );
    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<ApiResponseDTO<Void>> markAsRead(
            Authentication authentication,
            @PathVariable String notificationId) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        log.info("Mark notification as read request for user: {} notification: {}",
                user.getEmail(), notificationId);

        notificationService.markAsRead(
                user.getId(),
                notificationId
        );

        return ResponseEntity.ok(
                ApiResponseDTO.success("Notification marked as read", null)
        );
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponseDTO<Void>> markAllAsRead(
            Authentication authentication) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        log.info("Mark all notifications as read request for user: {}", user.getEmail());

        notificationService.markAllAsRead(user.getId());

        return ResponseEntity.ok(
                ApiResponseDTO.success("All notifications marked as read", null)
        );
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete notification")
    public ResponseEntity<ApiResponseDTO<Void>> deleteNotification(
            Authentication authentication,
            @PathVariable String notificationId) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        log.info("Delete notification request for user: {} notification: {}",
                user.getEmail(), notificationId);

        notificationService.deleteNotification(
                user.getId(),
                notificationId
        );

        return ResponseEntity.ok(
                ApiResponseDTO.success("Notification deleted successfully", null)
        );
    }

    @DeleteMapping("/delete-all")
    @Operation(summary = "Delete all user notifications")
    public ResponseEntity<ApiResponseDTO<Void>> deleteAllNotifications(
            Authentication authentication) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        log.info("Delete all notifications request for user: {}", user.getEmail());

        notificationService.deleteAllUserNotifications(user.getId());

        return ResponseEntity.ok(
                ApiResponseDTO.success("All notifications deleted successfully", null)
        );
    }

    @GetMapping("/count/unread")
    @Operation(summary = "Get unread notifications count")
    public ResponseEntity<ApiResponseDTO<Long>> getUnreadCount(
            Authentication authentication) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        log.info("Get unread notifications count request for user: {}", user.getEmail());

        long count = notificationService.countUnreadNotifications(user.getId());

        return ResponseEntity.ok(
                ApiResponseDTO.success("Unread count retrieved", count)
        );
    }
}