package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.dto.response.common.NotificationDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.domain.enums.NotificationType;

import java.util.List;

public interface NotificationService {

    NotificationDTO createNotification(String userId, String title, String message, NotificationType type);

    NotificationDTO createNotificationWithEntity(String userId, String title, String message, NotificationType type, String entityId, String entityType);



    PaginatedResponseDTO<NotificationDTO> getUserNotifications(String userId, int page, int size);

    List<NotificationDTO> getUserUnreadNotifications(String userId);

    NotificationDTO getNotificationById(
            String userId,
            String notificationId
    );

    void markAsRead(
            String userId,
            String notificationId
    );

    void deleteNotification(
            String userId,
            String notificationId
    );

    void markAllAsRead(String userId);



    void deleteAllUserNotifications(String userId);

    long countUnreadNotifications(String userId);

    void sendOrderUpdateNotification(String userId, String orderId, String status);

    void sendPaymentNotification(String userId, String orderId, String paymentStatus);

    void sendDriverAssignmentNotification(String userId, String orderId, String driverName);

    void sendSystemNotification(String userId, String title, String message);
}