package com.inkfront.logisticsApplication.service.impl.payment;

import com.inkfront.logisticsApplication.domain.entity.Order;
import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import com.inkfront.logisticsApplication.exception.PaymentAlreadyCompletedException;
import com.inkfront.logisticsApplication.exception.ResourceNotFoundException;
import com.inkfront.logisticsApplication.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPaymentService {

    private final OrderRepository orderRepository;

    public Order getOrderForPayment(String orderId, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You do not own this order");
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new PaymentAlreadyCompletedException("Order is already paid");
        }

        return order;
    }

    public void updateOrderPaymentStatus(Order order, PaymentStatus status) {
        order.setPaymentStatus(status);
        orderRepository.save(order);
        log.info("Updated order {} payment status to {}", order.getId(), status);
    }
}