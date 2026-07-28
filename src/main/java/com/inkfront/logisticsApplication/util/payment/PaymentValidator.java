package com.inkfront.logisticsApplication.util.payment;

import com.inkfront.logisticsApplication.domain.entity.Order;
import com.inkfront.logisticsApplication.domain.entity.PaymentTransaction;
import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import com.inkfront.logisticsApplication.dto.request.payment.InitializePaymentRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentValidator {

    public PaymentTransaction buildInitialTransaction(InitializePaymentRequestDTO request, Order order, String reference) {
        PaymentTransaction tx = new PaymentTransaction();
        tx.setOrder(order);
        tx.setUser(order.getUser());
        tx.setTransactionReference(reference);
        tx.setAmount(BigDecimal.valueOf(request.getAmount()));
        tx.setCurrency(request.getCurrency() != null ? request.getCurrency() : "NGN");
        tx.setPaymentMethod(request.getPaymentMethod());
        tx.setGateway(request.getGateway());
        tx.setStatus(PaymentStatus.PENDING);
        tx.setCallbackUrl(request.getCallbackUrl());
        tx.setMaxRetries(3);
        tx.setRetryCount(0);
        if (request.getMetadata() != null) {
            // Convert map to JSON string (handled by object mapper elsewhere)
        }
        return tx;
    }
}