package com.inkfront.logisticsApplication.service.impl.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkfront.logisticsApplication.domain.entity.PaymentTransaction;
import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import com.inkfront.logisticsApplication.exception.PaymentNotFoundException;
import com.inkfront.logisticsApplication.repository.PaymentTransactionRepository;
import com.inkfront.logisticsApplication.util.payment.PaymentStateValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaystackWebhookService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentStateValidator stateValidator;
    private final OrderPaymentService orderPaymentService;
    private final PaymentNotificationService notificationService;
    private final ObjectMapper objectMapper;

    private static final String PAYSTACK_SECRET_KEY = "your_paystack_secret_key"; // should be injected from properties

    public void processWebhook(String payload, String signature) {
        // 1. Verify signature
        if (!verifySignature(payload, signature)) {
            log.warn("Invalid Paystack webhook signature");
            return;
        }

        try {
            JsonNode eventData = objectMapper.readTree(payload);
            String event = eventData.path("event").asText();
            JsonNode data = eventData.path("data");

            if ("charge.success".equals(event)) {
                handleChargeSuccess(data);
            } else if ("charge.failed".equals(event)) {
                handleChargeFailed(data);
            } else if ("refund.processed".equals(event)) {
                handleRefundProcessed(data);
            } else {
                log.info("Ignoring unsupported webhook event: {}", event);
            }
        } catch (Exception e) {
            log.error("Failed to process Paystack webhook", e);
        }
    }

    @Transactional
    protected void handleChargeSuccess(JsonNode data) {
        String reference = data.path("reference").asText();
        PaymentTransaction transaction = paymentTransactionRepository
                .findByTransactionReference(reference)
                .orElseThrow(() -> new PaymentNotFoundException("Transaction not found: " + reference));

        if (transaction.isCompleted()) {
            log.info("Webhook ignored – transaction already completed: {}", reference);
            return;
        }

        stateValidator.transition(transaction, PaymentStatus.PAID);
        transaction.setGatewayResponse(data.toString());
        transaction.setPaymentDate(java.time.LocalDateTime.now());
        transaction.setVerifiedAt(java.time.LocalDateTime.now());
        transaction.setGatewayReference(data.path("transaction").asText());
        paymentTransactionRepository.save(transaction);

        orderPaymentService.updateOrderPaymentStatus(transaction.getOrder(), PaymentStatus.PAID);
        notificationService.sendPaymentSuccessNotification(transaction);

        log.info("Webhook: charge.success processed for transaction: {}", reference);
    }

    @Transactional
    protected void handleChargeFailed(JsonNode data) {
        // similar logic
    }

    @Transactional
    protected void handleRefundProcessed(JsonNode data) {
        // similar logic
    }

    private boolean verifySignature(String payload, String signature) {
        try {
            Mac sha512Hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec keySpec = new SecretKeySpec(
                    PAYSTACK_SECRET_KEY.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA512"
            );
            sha512Hmac.init(keySpec);
            byte[] macData = sha512Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String computed = bytesToHex(macData);
            return computed.equals(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Signature verification failed", e);
            return false;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}