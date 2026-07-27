package com.inkfront.logisticsApplication.listener;

import com.inkfront.logisticsApplication.domain.entity.Order;
import com.inkfront.logisticsApplication.domain.entity.Notification;
import com.inkfront.logisticsApplication.domain.enums.NotificationType;
import com.inkfront.logisticsApplication.domain.enums.OrderStatus;
import com.inkfront.logisticsApplication.repository.NotificationRepository;
import com.inkfront.logisticsApplication.service.interfaces.EmailService;
import com.inkfront.logisticsApplication.service.interfaces.SmsService;
import com.inkfront.logisticsApplication.domain.entity.User;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(Order order) {
        log.info("Order created event received for order: {}", order.getOrderNumber());

        // Create notification for user
        createNotification(
                order.getUser().getId(),
                "Order Created",
                "Your order " + order.getOrderNumber() + " has been created successfully.",
                NotificationType.ORDER_UPDATE,
                order.getId()
        );

        // Send email
        emailService.sendOrderConfirmationEmail(
                order.getUser().getEmail(),
                order.getOrderNumber(),
                buildOrderDetailsEmail(order)
        );

        // Send SMS
        if (order.getUser().getPhoneNumber() != null) {
            smsService.sendOrderUpdateSms(
                    order.getUser().getPhoneNumber(),
                    order.getOrderNumber(),
                    "CREATED"
            );
        }
    }

    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderStatusChanged(OrderStatusChangeEvent event) {
        Order order = event.getOrder();
        OrderStatus oldStatus = event.getOldStatus();
        OrderStatus newStatus = event.getNewStatus();

        log.info("Order status changed from {} to {} for order: {}",
                oldStatus, newStatus, order.getOrderNumber());

        // Create notification
        String message = String.format("Your order %s status has been updated from %s to %s",
                order.getOrderNumber(), oldStatus.getDisplayName(), newStatus.getDisplayName());

        createNotification(
                order.getUser().getId(),
                "Order Status Updated",
                message,
                NotificationType.ORDER_UPDATE,
                order.getId()
        );

        // Send email
        emailService.sendOrderUpdateEmail(
                order.getUser().getEmail(),
                order.getOrderNumber(),
                newStatus.name()
        );

        // Send SMS
        if (order.getUser().getPhoneNumber() != null) {
            smsService.sendOrderUpdateSms(
                    order.getUser().getPhoneNumber(),
                    order.getOrderNumber(),
                    newStatus.name()
            );
        }

        // Handle specific status transitions
        switch (newStatus) {
            case ASSIGNED:
                handleOrderAssigned(order);
                break;
            case PICKED_UP:
                handleOrderPickedUp(order);
                break;
            case IN_TRANSIT:
                handleOrderInTransit(order);
                break;
            case DELIVERED:
                handleOrderDelivered(order);
                break;
            case CANCELLED:
                handleOrderCancelled(order);
                break;
        }
    }

    private void handleOrderAssigned(Order order) {
        if (order.getDriver() != null) {
            // Notify driver
            createNotification(
                    order.getDriver().getId(),
                    "New Order Assigned",
                    "You have been assigned to order " + order.getOrderNumber(),
                    NotificationType.ORDER_UPDATE,
                    order.getId()
            );

            // Send SMS to driver
            if (order.getDriver().getPhoneNumber() != null) {
                smsService.sendDriverAssignmentSms(
                        order.getDriver().getPhoneNumber(),
                        order.getOrderNumber(),
                        "Order assigned to you",
                        ""
                );
            }
        }
    }

    private void handleOrderPickedUp(Order order) {
        log.info("Order picked up: {}", order.getOrderNumber());
    }

    private void handleOrderInTransit(Order order) {
        log.info("Order in transit: {}", order.getOrderNumber());
    }

    private void handleOrderDelivered(Order order) {
        log.info("Order delivered: {}", order.getOrderNumber());

        // Create delivery notification
        createNotification(
                order.getUser().getId(),
                "Order Delivered",
                "Your order " + order.getOrderNumber() + " has been delivered successfully.",
                NotificationType.ORDER_UPDATE,
                order.getId()
        );

        // Send delivery confirmation
        smsService.sendDeliveryConfirmationSms(
                order.getUser().getPhoneNumber(),
                order.getOrderNumber()
        );

        // Update driver rating if needed
        // This would be triggered separately
    }

    private void handleOrderCancelled(Order order) {
        log.info("Order cancelled: {}", order.getOrderNumber());

        // Notify driver if assigned
        if (order.getDriver() != null) {
            createNotification(
                    order.getDriver().getId(),
                    "Order Cancelled",
                    "Order " + order.getOrderNumber() + " has been cancelled.",
                    NotificationType.ORDER_UPDATE,
                    order.getId()
            );
        }
    }

    private void createNotification(String userId, String title, String message,
                                    NotificationType type, String relatedEntityId) {
        Notification notification = new Notification();
        notification.setUser(new User()); // Need to fetch user entity
        User user = new User();
        user.setId(userId);
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setRelatedEntityType("ORDER");
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }

    private String buildOrderDetailsEmail(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("Order Details:\n");
        sb.append("Order Number: ").append(order.getOrderNumber()).append("\n");
        sb.append("Pickup Location: ").append(order.getPickupLocation()).append("\n");
        sb.append("Delivery Location: ").append(order.getDeliveryLocation()).append("\n");
        sb.append("Distance: ").append(order.getDistanceKm()).append(" km\n");
        sb.append("Total Price: ₦").append(String.format("%.2f", order.getTotalPrice())).append("\n");
        sb.append("Status: ").append(order.getStatus().getDisplayName()).append("\n");
        sb.append("Estimated Delivery: ").append(order.getEstimatedDeliveryDate()).append("\n");
        return sb.toString();
    }
}
