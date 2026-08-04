package com.inkfront.logisticsApplication.listener;

import com.inkfront.logisticsApplication.domain.entity.Order;
import com.inkfront.logisticsApplication.dto.request.dispatch.DispatchRequestDTO;
import com.inkfront.logisticsApplication.events.order.OrderReadyForDispatchEvent;
import com.inkfront.logisticsApplication.service.interfaces.dispatch.DispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderReadyForDispatchEventListener {

    private final DispatchService dispatchService;

    @Async
    @EventListener
    public void handleOrderReadyForDispatch(OrderReadyForDispatchEvent event) {
        Order order = event.getOrder();
        log.info("Order ready for dispatch: {}", order.getOrderNumber());

        try {
            DispatchRequestDTO request = DispatchRequestDTO.builder()
                    .orderId(order.getId())
                    .autoAssign(false)
                    .priority(0)
                    .notes("Auto-created from payment success")
                    .build();

            dispatchService.createDispatch(request, "SYSTEM");
            log.info("Dispatch auto-created for order: {}", order.getOrderNumber());

        } catch (Exception e) {
            log.error("Failed to auto-create dispatch for order {}: {}",
                    order.getOrderNumber(), e.getMessage(), e);
        }
    }
}