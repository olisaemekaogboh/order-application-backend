package com.inkfront.logisticsApplication.events;

import com.inkfront.logisticsApplication.domain.entity.PaymentTransaction;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PaymentRefundedEvent extends ApplicationEvent {
    private final PaymentTransaction transaction;

    public PaymentRefundedEvent(Object source, PaymentTransaction transaction) {
        super(source);
        this.transaction = transaction;
    }
}
