package com.inkfront.logisticsApplication.events.publisher;

import com.inkfront.logisticsApplication.domain.entity.PaymentTransaction;
import com.inkfront.logisticsApplication.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishPaymentCompleted(PaymentTransaction transaction) {
        eventPublisher.publishEvent(new PaymentCompletedEvent(this, transaction));
        log.info("Published PaymentCompletedEvent for transaction: {}", transaction.getTransactionReference());
    }

    public void publishPaymentFailed(PaymentTransaction transaction) {
        eventPublisher.publishEvent(new PaymentFailedEvent(this, transaction));
        log.info("Published PaymentFailedEvent for transaction: {}", transaction.getTransactionReference());
    }

    public void publishPaymentRefunded(PaymentTransaction transaction) {
        eventPublisher.publishEvent(new PaymentRefundedEvent(this, transaction));
        log.info("Published PaymentRefundedEvent for transaction: {}", transaction.getTransactionReference());
    }

    public void publishPaymentInitialized(PaymentTransaction transaction) {
        eventPublisher.publishEvent(new PaymentInitializedEvent(this, transaction));
        log.info("Published PaymentInitializedEvent for transaction: {}", transaction.getTransactionReference());
    }
}