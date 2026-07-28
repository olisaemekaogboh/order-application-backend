package com.inkfront.logisticsApplication.service.impl.payment;

import com.inkfront.logisticsApplication.domain.entity.PaymentTransaction;
import com.inkfront.logisticsApplication.service.interfaces.EmailService;
import com.inkfront.logisticsApplication.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentNotificationService {

    private final NotificationService notificationService;
    private final EmailService emailService;

    public void sendPaymentSuccessNotification(PaymentTransaction transaction) {
        String userId = transaction.getOrder().getUser().getId();
        String orderId = transaction.getOrder().getId();
        notificationService.sendPaymentNotification(userId, orderId, "PAID");
        emailService.sendPaymentConfirmationEmail(
                transaction.getOrder().getUser().getEmail(),
                orderId,
                "Payment successful for order " + transaction.getOrder().getOrderNumber()
        );
        log.info("Payment success notification sent for transaction: {}", transaction.getTransactionReference());
    }

    public void sendPaymentRefundedNotification(PaymentTransaction transaction) {
        String userId = transaction.getOrder().getUser().getId();
        notificationService.sendPaymentNotification(userId, transaction.getOrder().getId(), "REFUNDED");
        log.info("Payment refund notification sent for transaction: {}", transaction.getTransactionReference());
    }

    public void sendPaymentFailedNotification(PaymentTransaction transaction) {
        String userId = transaction.getOrder().getUser().getId();
        notificationService.sendPaymentNotification(userId, transaction.getOrder().getId(), "FAILED");
        log.info("Payment failed notification sent for transaction: {}", transaction.getTransactionReference());
    }
}