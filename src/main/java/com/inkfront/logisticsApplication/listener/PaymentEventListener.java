package com.inkfront.logisticsApplication.listener;

import com.inkfront.logisticsApplication.domain.entity.PaymentTransaction;
import com.inkfront.logisticsApplication.service.interfaces.EmailService;
import com.inkfront.logisticsApplication.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final NotificationService notificationService;
    private final EmailService emailService;

    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentSuccess(PaymentTransaction payment) {
        log.info("Payment success event received for transaction: {}", payment.getTransactionReference());

        // Notify user
        notificationService.sendPaymentNotification(
                payment.getOrder().getUser().getId(),
                payment.getOrder().getId(),
                "SUCCESS"
        );

        // Send email
        emailService.sendPaymentConfirmationEmail(
                payment.getOrder().getUser().getEmail(),
                payment.getOrder().getOrderNumber(),
                String.valueOf(payment.getAmount())
        );
    }

    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentFailure(PaymentTransaction payment) {
        log.warn("Payment failure event received for transaction: {}", payment.getTransactionReference());

        // Notify user
        notificationService.sendPaymentNotification(
                payment.getOrder().getUser().getId(),
                payment.getOrder().getId(),
                "FAILED"
        );
    }

    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentRefund(PaymentTransaction payment) {
        log.info("Payment refund event received for transaction: {}", payment.getTransactionReference());

        // Notify user
        notificationService.sendPaymentNotification(
                payment.getOrder().getUser().getId(),
                payment.getOrder().getId(),
                "REFUNDED"
        );
    }
}