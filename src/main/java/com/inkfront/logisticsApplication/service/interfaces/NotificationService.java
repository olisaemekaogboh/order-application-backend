package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.dto.response.common.NotificationDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.domain.enums.NotificationType;

import java.util.List;

public interface NotificationService {

    NotificationDTO createNotification(String userId, String title, String message, NotificationType type);

    NotificationDTO createNotificationWithEntity(String userId, String title, String message, NotificationType type, String entityId, String entityType);

    NotificationDTO getNotificationById(String notificationId);

    PaginatedResponseDTO<NotificationDTO> getUserNotifications(String userId, int page, int size);

    List<NotificationDTO> getUserUnreadNotifications(String userId);

    void markAsRead(String notificationId);

    void markAllAsRead(String userId);

    void deleteNotification(String notificationId);

    void deleteAllUserNotifications(String userId);

    long countUnreadNotifications(String userId);

    void sendOrderUpdateNotification(String userId, String orderId, String status);

    void sendPaymentNotification(String userId, String orderId, String paymentStatus);

    void sendDriverAssignmentNotification(String userId, String orderId, String driverName);

    void sendSystemNotification(String userId, String title, String message);
}