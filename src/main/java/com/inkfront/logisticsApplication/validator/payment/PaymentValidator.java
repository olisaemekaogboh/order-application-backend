package com.inkfront.logisticsApplication.validator.payment;

import com.inkfront.logisticsApplication.domain.entity.Order;
import com.inkfront.logisticsApplication.domain.entity.PaymentTransaction;
import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import com.inkfront.logisticsApplication.dto.request.payment.InitializePaymentRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentValidator {

    private final ObjectMapper objectMapper;

    public PaymentTransaction buildInitialTransaction(InitializePaymentRequestDTO request, Order order, String reference) {
        PaymentTransaction tx = new PaymentTransaction();
        tx.setOrder(order);
        tx.setUser(order.getUser());
        tx.setTransactionReference(reference);
        // FIX: Get amount from order, not from request
        tx.setAmount(BigDecimal.valueOf(order.getTotalPrice()));
        tx.setCurrency(request.getCurrency() != null ? request.getCurrency() : "NGN");
        tx.setPaymentMethod(request.getPaymentMethod());
        tx.setGateway(request.getGateway());
        tx.setStatus(PaymentStatus.PENDING);
        tx.setCallbackUrl(request.getCallbackUrl());
        tx.setMaxRetries(3);
        tx.setRetryCount(0);

        // Handle metadata as JSON string
        if (request.getMetadata() != null) {
            try {
                tx.setMetadata(objectMapper.writeValueAsString(request.getMetadata()));
            } catch (Exception e) {
                log.warn("Failed to serialize metadata to JSON", e);
            }
        }

        return tx;
    }
}