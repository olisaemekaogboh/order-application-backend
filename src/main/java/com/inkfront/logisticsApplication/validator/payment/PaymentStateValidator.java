package com.inkfront.logisticsApplication.validator.payment;

import com.inkfront.logisticsApplication.domain.entity.PaymentTransaction;
import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import com.inkfront.logisticsApplication.exception.InvalidPaymentStateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class PaymentStateValidator {

    private static final Map<PaymentStatus, Set<PaymentStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(PaymentStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(PaymentStatus.PENDING, EnumSet.of(PaymentStatus.PROCESSING, PaymentStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(PaymentStatus.PROCESSING, EnumSet.of(PaymentStatus.PAID, PaymentStatus.FAILED, PaymentStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(PaymentStatus.PAID, EnumSet.of(PaymentStatus.REFUND_PENDING, PaymentStatus.REFUNDED));
        ALLOWED_TRANSITIONS.put(PaymentStatus.REFUND_PENDING, EnumSet.of(PaymentStatus.REFUNDED, PaymentStatus.FAILED));
        ALLOWED_TRANSITIONS.put(PaymentStatus.FAILED, EnumSet.noneOf(PaymentStatus.class));
        ALLOWED_TRANSITIONS.put(PaymentStatus.REFUNDED, EnumSet.noneOf(PaymentStatus.class));
        ALLOWED_TRANSITIONS.put(PaymentStatus.CANCELLED, EnumSet.noneOf(PaymentStatus.class));
    }

    public void validateTransition(PaymentStatus from, PaymentStatus to) {
        Set<PaymentStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(from, EnumSet.noneOf(PaymentStatus.class));
        if (!allowed.contains(to)) {
            throw new InvalidPaymentStateException(
                    String.format("Invalid transition from %s to %s", from, to)
            );
        }
        log.debug("Validated transition from {} to {}", from, to);
    }

    public PaymentTransaction transition(PaymentTransaction transaction, PaymentStatus newStatus) {
        PaymentStatus oldStatus = transaction.getStatus();
        validateTransition(oldStatus, newStatus);
        transaction.setStatus(newStatus);
        if (newStatus == PaymentStatus.PAID) {
            transaction.setPaymentDate(LocalDateTime.now());
            transaction.setVerifiedAt(LocalDateTime.now());
        } else if (newStatus == PaymentStatus.REFUNDED) {
            transaction.setRefundedAt(LocalDateTime.now());
        } else if (newStatus == PaymentStatus.FAILED || newStatus == PaymentStatus.CANCELLED) {
            if (transaction.getFailureReason() == null) {
                transaction.setFailureReason("Status changed to " + newStatus);
            }
        }
        return transaction;
    }
}