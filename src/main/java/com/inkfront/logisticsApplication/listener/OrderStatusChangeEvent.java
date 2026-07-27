
// OrderStatusChangeEvent.java
package com.inkfront.logisticsApplication.listener;

import com.inkfront.logisticsApplication.domain.entity.Order;
import com.inkfront.logisticsApplication.domain.enums.OrderStatus;
import lombok.Getter;

@Getter
public class OrderStatusChangeEvent {
    private final Order order;
    private final OrderStatus oldStatus;
    private final OrderStatus newStatus;

    public OrderStatusChangeEvent(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        this.order = order;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }
}