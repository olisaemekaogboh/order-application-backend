package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.dto.request.notification.*;
import com.inkfront.logisticsApplication.dto.response.common.NotificationDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.notification.NotificationPreferenceResponseDTO;
import com.inkfront.logisticsApplication.domain.enums.NotificationType;

import java.util.List;

public interface NotificationService {

    // --- New methods using DTOs ---
    NotificationDTO sendNotification(NotificationRequestDTO request);

    NotificationPreferenceResponseDTO updatePreferences(String userId, NotificationPreferenceRequestDTO request);

    void broadcastNotification(BroadcastNotificationRequestDTO request);

    // ✅ FIXED: markAsRead with userId and notificationId
    NotificationDTO markAsRead(String userId, String notificationId);

    // ✅ NEW: markAsUnread
    NotificationDTO markAsUnread(String userId, String notificationId);

    // ✅ Deprecated: markAsRead with DTO (kept for backward compatibility)
    @Deprecated
    NotificationDTO markAsRead(String notificationId, NotificationReadRequestDTO request);

    // --- Legacy methods (kept for backward compatibility; delegate to new ones) ---
    @Deprecated
    NotificationDTO createNotification(String userId, String title, String message, NotificationType type);

    @Deprecated
    NotificationDTO createNotificationWithEntity(String userId, String title, String message,
                                                 NotificationType type, String entityId, String entityType);

    // --- Existing methods (unchanged) ---
    PaginatedResponseDTO<NotificationDTO> getUserNotifications(String userId, int page, int size);

    List<NotificationDTO> getUserUnreadNotifications(String userId);

    NotificationDTO getNotificationById(String userId, String notificationId);

    void markAllAsRead(String userId);

    void deleteNotification(String userId, String notificationId);

    void deleteAllUserNotifications(String userId);

    long countUnreadNotifications(String userId);

    // Convenience methods (unchanged, will use sendNotification internally)
    void sendOrderUpdateNotification(String userId, String orderId, String status);

    void sendPaymentNotification(String userId, String orderId, String paymentStatus);

    void sendDriverAssignmentNotification(String userId, String orderId, String driverName);

    void sendSystemNotification(String userId, String title, String message);
}