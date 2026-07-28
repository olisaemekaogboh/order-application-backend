package com.inkfront.logisticsApplication.service.impl.payment.flutterwave;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkfront.logisticsApplication.config.payment.FlutterwaveProperties;
import com.inkfront.logisticsApplication.domain.entity.PaymentTransaction;
import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import com.inkfront.logisticsApplication.exception.PaymentNotFoundException;
import com.inkfront.logisticsApplication.repository.PaymentTransactionRepository;
import com.inkfront.logisticsApplication.service.impl.payment.OrderPaymentService;
import com.inkfront.logisticsApplication.service.impl.payment.PaymentNotificationService;
import com.inkfront.logisticsApplication.validator.payment.PaymentStateValidator;
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
public class FlutterwaveWebhookService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentStateValidator stateValidator;
    private final OrderPaymentService orderPaymentService;
    private final PaymentNotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final FlutterwaveProperties flutterwaveProperties;

    public void processWebhook(String payload, String signature) {
        if (!verifySignature(payload, signature)) {
            log.warn("Invalid Flutterwave webhook signature");
            return;
        }

        try {
            JsonNode eventData = objectMapper.readTree(payload);
            String event = eventData.path("event").asText();
            JsonNode data = eventData.path("data");

            if ("charge.completed".equals(event)) {
                handleChargeCompleted(data);
            } else if ("refund.completed".equals(event)) {
                handleRefundCompleted(data);
            } else {
                log.info("Ignoring unsupported Flutterwave webhook event: {}", event);
            }
        } catch (Exception e) {
            log.error("Failed to process Flutterwave webhook", e);
        }
    }

    @Transactional
    protected void handleChargeCompleted(JsonNode data) {
        String reference = data.path("tx_ref").asText();
        PaymentTransaction transaction = paymentTransactionRepository
                .findByTransactionReference(reference)
                .orElseThrow(() -> new PaymentNotFoundException("Transaction not found: " + reference));

        if (transaction.isCompleted()) {
            log.info("Webhook ignored – transaction already completed: {}", reference);
            return;
        }

        String status = data.path("status").asText();
        PaymentStatus newStatus = "successful".equalsIgnoreCase(status) ? PaymentStatus.PAID : PaymentStatus.FAILED;

        stateValidator.transition(transaction, newStatus);
        transaction.setGatewayReference(data.path("flw_ref").asText());
        transaction.setGatewayResponse(data.toString());
        if (newStatus == PaymentStatus.PAID) {
            transaction.setPaymentDate(java.time.LocalDateTime.now());
            transaction.setVerifiedAt(java.time.LocalDateTime.now());
        } else {
            transaction.setFailureReason(data.path("processor_response").asText("Payment failed"));
        }
        paymentTransactionRepository.save(transaction);

        if (newStatus == PaymentStatus.PAID) {
            orderPaymentService.updateOrderPaymentStatus(transaction.getOrder(), PaymentStatus.PAID);
            notificationService.sendPaymentSuccessNotification(transaction);
        } else {
            notificationService.sendPaymentFailedNotification(transaction);
        }

        log.info("Webhook: charge.completed processed for transaction: {}", reference);
    }

    @Transactional
    protected void handleRefundCompleted(JsonNode data) {
        // Similar logic for refund
        String reference = data.path("tx_ref").asText();
        PaymentTransaction transaction = paymentTransactionRepository
                .findByTransactionReference(reference)
                .orElseThrow(() -> new PaymentNotFoundException("Transaction not found: " + reference));

        if (transaction.getStatus() == PaymentStatus.REFUNDED) {
            log.info("Refund already processed for transaction: {}", reference);
            return;
        }

        stateValidator.transition(transaction, PaymentStatus.REFUNDED);
        transaction.setGatewayResponse(data.toString());
        transaction.setRefundedAt(java.time.LocalDateTime.now());
        paymentTransactionRepository.save(transaction);

        orderPaymentService.updateOrderPaymentStatus(transaction.getOrder(), PaymentStatus.REFUNDED);
        notificationService.sendPaymentRefundedNotification(transaction);

        log.info("Webhook: refund.completed processed for transaction: {}", reference);
    }

    private boolean verifySignature(String payload, String signature) {
        try {
            Mac sha512Hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec keySpec = new SecretKeySpec(
                    flutterwaveProperties.getSecretKey().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA512"
            );
            sha512Hmac.init(keySpec);
            byte[] macData = sha512Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(macData).equals(signature);
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