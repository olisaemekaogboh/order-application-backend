package com.inkfront.logisticsApplication.service.impl;

import com.inkfront.logisticsApplication.domain.entity.Notification;
import com.inkfront.logisticsApplication.domain.entity.User;
import com.inkfront.logisticsApplication.domain.enums.NotificationType;
import com.inkfront.logisticsApplication.dto.request.notification.*;
import com.inkfront.logisticsApplication.dto.response.common.NotificationDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.notification.NotificationPreferenceResponseDTO;
import com.inkfront.logisticsApplication.exception.ResourceNotFoundException;
import com.inkfront.logisticsApplication.mapper.NotificationMapper;
import com.inkfront.logisticsApplication.repository.NotificationRepository;
import com.inkfront.logisticsApplication.repository.UserRepository;
import com.inkfront.logisticsApplication.service.interfaces.EmailService;
import com.inkfront.logisticsApplication.service.interfaces.NotificationService;
import com.inkfront.logisticsApplication.service.interfaces.NotificationWebSocketService;
import com.inkfront.logisticsApplication.service.interfaces.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final EmailService emailService;
    private final SmsService smsService;
    private final NotificationWebSocketService notificationWebSocketService;

    // ===== New methods using DTOs =====

    @Override
    public NotificationDTO sendNotification(NotificationRequestDTO request) {
        log.info("Sending notification to user: {}", request.getRecipientId());

        if (request.getRecipientId() == null) {
            throw new IllegalArgumentException("Recipient ID is required");
        }

        User user = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType());
        notification.setRelatedEntityId(request.getRelatedEntityId());
        notification.setRelatedEntityType(request.getRelatedEntityType());
        notification.setCreatedAt(LocalDateTime.now());

        notification = notificationRepository.save(notification);

        NotificationDTO dto = notificationMapper.toDTO(notification);
        notificationWebSocketService.sendNotification(user.getId(), dto);

        return dto;
    }

    @Override
    public NotificationPreferenceResponseDTO updatePreferences(String userId, NotificationPreferenceRequestDTO request) {
        log.info("Updating notification preferences for user: {}", userId);

        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Since User entity does not have preference fields, we cannot persist them.
        // Return the requested preferences as a response (no persistence).
        log.warn("Notification preferences are not persisted because User entity lacks the required fields.");

        return NotificationPreferenceResponseDTO.builder()
                .emailEnabled(request.getEmailEnabled())
                .smsEnabled(request.getSmsEnabled())
                .pushEnabled(request.getPushEnabled())
                .build();
    }

    @Override
    public void broadcastNotification(BroadcastNotificationRequestDTO request) {
        log.info("Broadcasting notification to role: {}", request.getRecipientRole());

        List<User> recipients;
        if (request.getRecipientRole() != null) {
            recipients = userRepository.findByRole(request.getRecipientRole());
        } else {
            recipients = userRepository.findAll();
        }

        for (User user : recipients) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setTitle(request.getTitle());
            notification.setMessage(request.getMessage());
            notification.setType(request.getType());
            notification.setCreatedAt(LocalDateTime.now());

            notification = notificationRepository.save(notification);

            NotificationDTO dto = notificationMapper.toDTO(notification);
            notificationWebSocketService.sendNotification(user.getId(), dto);
        }

        log.info("Broadcasted to {} users", recipients.size());
    }

    @Override
    public NotificationDTO markAsRead(String notificationId, NotificationReadRequestDTO request) {
        log.info("Marking notification {} as read={}", notificationId, request.getRead());

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (request.getRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        } else {
            notification.setRead(false);
            notification.setReadAt(null);
        }

        notification = notificationRepository.save(notification);
        return notificationMapper.toDTO(notification);
    }

    // ===== Legacy methods (delegating to new ones) =====

    @Override
    @Deprecated
    public NotificationDTO createNotification(String userId, String title, String message, NotificationType type) {
        return createNotificationWithEntity(userId, title, message, type, null, null);
    }

    @Override
    @Deprecated
    public NotificationDTO createNotificationWithEntity(String userId, String title, String message,
                                                        NotificationType type, String entityId, String entityType) {
        NotificationRequestDTO request = NotificationRequestDTO.builder()
                .recipientId(userId)
                .title(title)
                .message(message)
                .type(type)
                .relatedEntityId(entityId)
                .relatedEntityType(entityType)
                .build();
        return sendNotification(request);
    }

    // ===== Existing methods (unchanged) =====

    @Override
    public NotificationDTO getNotificationById(String userId, String notificationId) {
        Notification notification = notificationRepository
                .findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        return notificationMapper.toDTO(notification);
    }

    @Override
    public PaginatedResponseDTO<NotificationDTO> getUserNotifications(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notifications = notificationRepository.findByUserId(userId, pageable);

        List<NotificationDTO> content = notifications.getContent().stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());

        return new PaginatedResponseDTO<>(content, notifications.getNumber(),
                notifications.getSize(), notifications.getTotalElements());
    }

    @Override
    public List<NotificationDTO> getUserUnreadNotifications(String userId) {
        return notificationRepository.findByUserIdAndReadFalse(userId).stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void markAllAsRead(String userId) {
        notificationRepository.markAllAsRead(userId, LocalDateTime.now());
    }

    @Override
    public void deleteNotification(String userId, String notificationId) {
        Notification notification = notificationRepository
                .findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notificationRepository.delete(notification);
    }

    @Override
    public void deleteAllUserNotifications(String userId) {
        Page<Notification> page = notificationRepository.findByUserId(userId, Pageable.unpaged());
        notificationRepository.deleteAll(page.getContent());
    }

    @Override
    public long countUnreadNotifications(String userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    public void sendOrderUpdateNotification(String userId, String orderId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String title = "Order Update";
        String message = "Your order #" + orderId + " status has been updated to: " + status;

        createNotificationWithEntity(userId, title, message, NotificationType.ORDER_UPDATE, orderId, "ORDER");

        emailService.sendOrderUpdateEmail(user.getEmail(), orderId, status);
        if (user.getPhoneNumber() != null) {
            smsService.sendOrderUpdateSms(user.getPhoneNumber(), orderId, status);
        }
    }

    @Override
    public void sendPaymentNotification(String userId, String orderId, String paymentStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String title = "Payment Update";
        String message = "Payment for order #" + orderId + " is " + paymentStatus;

        createNotificationWithEntity(userId, title, message, NotificationType.PAYMENT, orderId, "ORDER");

        emailService.sendPaymentConfirmationEmail(user.getEmail(), orderId, "Payment " + paymentStatus);
    }

    @Override
    public void sendDriverAssignmentNotification(String userId, String orderId, String driverName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String title = "Driver Assigned";
        String message = "Driver " + driverName + " has been assigned to your order #" + orderId;

        createNotificationWithEntity(userId, title, message, NotificationType.ORDER_UPDATE, orderId, "ORDER");

        emailService.sendDriverAssignmentEmail(user.getEmail(), orderId, driverName, "");
        if (user.getPhoneNumber() != null) {
            smsService.sendDriverAssignmentSms(user.getPhoneNumber(), orderId, driverName, "");
        }
    }

    @Override
    public void sendSystemNotification(String userId, String title, String message) {
        createNotification(userId, title, message, NotificationType.SYSTEM);
    }
}