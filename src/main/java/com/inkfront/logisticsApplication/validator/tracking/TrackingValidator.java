package com.inkfront.logisticsApplication.validator.tracking;

import com.inkfront.logisticsApplication.domain.entity.Order;
import com.inkfront.logisticsApplication.domain.entity.Driver;
import com.inkfront.logisticsApplication.exception.ResourceNotFoundException;
import com.inkfront.logisticsApplication.repository.OrderRepository;
import com.inkfront.logisticsApplication.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrackingValidator {

    private final OrderRepository orderRepository;
    private final DriverRepository driverRepository;

    public Order validateOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }

    public Driver validateDriver(String driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + driverId));
    }

    public void validateOrderNotAlreadyTracked(Order order) {
        // In a real implementation, you'd check if a tracking session already exists for this order
        // We'll rely on the service to check via repository.
    }
}