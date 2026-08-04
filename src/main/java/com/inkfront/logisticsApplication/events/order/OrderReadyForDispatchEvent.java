package com.inkfront.logisticsApplication.events.order;

import com.inkfront.logisticsApplication.domain.entity.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderReadyForDispatchEvent extends ApplicationEvent {

    private final Order order;
    private final String userId;

    public OrderReadyForDispatchEvent(Object source, Order order, String userId) {
        super(source);
        this.order = order;
        this.userId = userId;
    }
}