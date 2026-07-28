// PaymentCompletedEvent.java
package com.inkfront.logisticsApplication.events;

import com.inkfront.logisticsApplication.domain.entity.PaymentTransaction;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PaymentCompletedEvent extends ApplicationEvent {
    private final PaymentTransaction transaction;
    public PaymentCompletedEvent(Object source, PaymentTransaction transaction) {
        super(source);
        this.transaction = transaction;
    }
}



