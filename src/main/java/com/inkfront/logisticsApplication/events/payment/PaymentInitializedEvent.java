package com.inkfront.logisticsApplication.events.payment;

import com.inkfront.logisticsApplication.domain.entity.PaymentTransaction;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PaymentInitializedEvent extends ApplicationEvent {
    private final PaymentTransaction transaction;

    public PaymentInitializedEvent(Object source, PaymentTransaction transaction) {
        super(source);
        this.transaction = transaction;
    }
}
