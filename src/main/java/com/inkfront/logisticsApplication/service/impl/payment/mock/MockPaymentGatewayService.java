package com.inkfront.logisticsApplication.service.impl.payment.mock;

import com.inkfront.logisticsApplication.domain.entity.PaymentTransaction;
import com.inkfront.logisticsApplication.domain.enums.PaymentGateway;
import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import com.inkfront.logisticsApplication.dto.request.payment.InitializePaymentRequestDTO;
import com.inkfront.logisticsApplication.exception.PaymentGatewayException;
import com.inkfront.logisticsApplication.service.interfaces.payment.PaymentGatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class MockPaymentGatewayService implements PaymentGatewayService {

    @Override
    public PaymentGateway getGateway() {
        return PaymentGateway.MOCK;
    }

    @Override
    public PaymentTransaction initialize(InitializePaymentRequestDTO request, PaymentTransaction transaction) {
        log.info("Mock payment initialization for transaction: {}", transaction.getTransactionReference());
        transaction.setGatewayReference("MOCK_" + UUID.randomUUID());
        transaction.setAuthorizationUrl("https://mock-payment.com/authorize/" + transaction.getTransactionReference());
        transaction.setAccessCode("MOCK_ACCESS");
        transaction.setStatus(PaymentStatus.PROCESSING);
        return transaction;
    }

    @Override
    public PaymentTransaction verify(PaymentTransaction transaction, String gatewayReference) {
        log.info("Mock payment verification for transaction: {}", transaction.getTransactionReference());
        // Simulate verification: always success for MOCK
        transaction.setStatus(PaymentStatus.PAID);
        transaction.setGatewayReference(gatewayReference);
        transaction.setPaymentDate(LocalDateTime.now());
        transaction.setGatewayResponse("{\"status\":\"success\",\"message\":\"Payment verified\"}");
        return transaction;
    }

    @Override
    public PaymentTransaction refund(PaymentTransaction transaction, String reason) {
        log.info("Mock payment refund for transaction: {}", transaction.getTransactionReference());
        if (transaction.getStatus() != PaymentStatus.PAID) {
            throw new PaymentGatewayException("Only successful payments can be refunded");
        }
        transaction.setStatus(PaymentStatus.REFUNDED);
        transaction.setFailureReason("Refunded: " + reason);
        return transaction;
    }

    @Override
    public boolean supports(PaymentGateway gateway) {
        return gateway == PaymentGateway.MOCK;
    }
}