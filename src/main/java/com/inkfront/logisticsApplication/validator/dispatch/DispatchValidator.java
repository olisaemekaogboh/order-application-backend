package com.inkfront.logisticsApplication.validator.dispatch;

import com.inkfront.logisticsApplication.repository.dispatch.DispatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DispatchValidator {

    private final DispatchRepository dispatchRepository;

    public void validateOrderNotDispatched(String orderId) {
        if (dispatchRepository.findByOrderId(orderId).isPresent()) {
            throw new IllegalStateException("Order already has an active dispatch");
        }
    }
}