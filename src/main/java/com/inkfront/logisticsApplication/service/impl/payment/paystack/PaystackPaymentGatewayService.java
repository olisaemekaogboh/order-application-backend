package com.inkfront.logisticsApplication.service.impl.payment.paystack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkfront.logisticsApplication.config.payment.PaystackProperties;
import com.inkfront.logisticsApplication.domain.entity.PaymentTransaction;
import com.inkfront.logisticsApplication.domain.enums.PaymentGateway;
import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import com.inkfront.logisticsApplication.dto.request.payment.InitializePaymentRequestDTO;
import com.inkfront.logisticsApplication.exception.PaymentGatewayException;
import com.inkfront.logisticsApplication.service.interfaces.payment.PaymentGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaystackPaymentGatewayService implements PaymentGatewayService {

    private final PaystackProperties paystackProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String INITIALIZE_URL = "/transaction/initialize";
    private static final String VERIFY_URL = "/transaction/verify/%s";
    private static final String REFUND_URL = "/refund";

    @Override
    public PaymentGateway getGateway() {
        return PaymentGateway.PAYSTACK;
    }

    @Override
    public PaymentTransaction initialize(InitializePaymentRequestDTO request, PaymentTransaction transaction) {
        log.info("Initializing Paystack payment for transaction: {}", transaction.getTransactionReference());

        try {
            // Build request body
            Map<String, Object> payload = new HashMap<>();
            payload.put("email", getCustomerEmail(transaction));
            // Convert amount to kobo (multiply by 100) and get int value
            int amountInKobo = transaction.getAmount().multiply(BigDecimal.valueOf(100)).intValue();
            payload.put("amount", amountInKobo);
            payload.put("reference", transaction.getTransactionReference());
            payload.put("callback_url", request.getCallbackUrl() != null ? request.getCallbackUrl() : paystackProperties.getCallbackUrl());
            if (request.getMetadata() != null) {
                payload.put("metadata", request.getMetadata());
            }

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(payload, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    paystackProperties.getBaseUrl() + INITIALIZE_URL,
                    HttpMethod.POST,
                    httpEntity,
                    JsonNode.class
            );

            JsonNode body = response.getBody();
            if (body == null || !body.path("status").asBoolean(false)) {
                String message = body != null ? body.path("message").asText("Unknown error") : "Empty response";
                throw new PaymentGatewayException("Paystack initialization failed: " + message);
            }

            JsonNode data = body.path("data");
            transaction.setGatewayReference(data.path("reference").asText());
            transaction.setAuthorizationUrl(data.path("authorization_url").asText());
            transaction.setAccessCode(data.path("access_code").asText());
            transaction.setStatus(PaymentStatus.PROCESSING);
            transaction.setGatewayResponse(body.toString());

            log.info("Paystack payment initialized successfully: {}", transaction.getTransactionReference());
            return transaction;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Paystack API error during initialization: {}", e.getResponseBodyAsString(), e);
            throw new PaymentGatewayException("Paystack API error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during Paystack initialization", e);
            throw new PaymentGatewayException("Failed to initialize Paystack payment", e);
        }
    }

    @Override
    public PaymentTransaction verify(PaymentTransaction transaction, String gatewayReference) {
        log.info("Verifying Paystack payment for transaction: {}", transaction.getTransactionReference());

        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

            String url = String.format(paystackProperties.getBaseUrl() + VERIFY_URL, gatewayReference);
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    httpEntity,
                    JsonNode.class
            );

            JsonNode body = response.getBody();
            if (body == null || !body.path("status").asBoolean(false)) {
                String message = body != null ? body.path("message").asText("Unknown error") : "Empty response";
                throw new PaymentGatewayException("Paystack verification failed: " + message);
            }

            JsonNode data = body.path("data");
            String status = data.path("status").asText();

            PaymentStatus newStatus;
            switch (status.toLowerCase()) {
                case "success":
                    newStatus = PaymentStatus.PAID;
                    break;
                case "failed":
                    newStatus = PaymentStatus.FAILED;
                    break;
                case "pending":
                    newStatus = PaymentStatus.PENDING;
                    break;
                default:
                    newStatus = PaymentStatus.PENDING;
            }

            transaction.setStatus(newStatus);
            transaction.setGatewayReference(gatewayReference);
            transaction.setGatewayResponse(body.toString());
            if (newStatus == PaymentStatus.PAID) {
                transaction.setPaymentDate(LocalDateTime.now());
            } else if (newStatus == PaymentStatus.FAILED) {
                transaction.setFailureReason(data.path("gateway_response").asText("Payment failed"));
            }

            log.info("Paystack verification completed: {} -> {}", transaction.getTransactionReference(), newStatus);
            return transaction;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Paystack API error during verification: {}", e.getResponseBodyAsString(), e);
            throw new PaymentGatewayException("Paystack API error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during Paystack verification", e);
            throw new PaymentGatewayException("Failed to verify Paystack payment", e);
        }
    }

    @Override
    public PaymentTransaction refund(PaymentTransaction transaction, String reason) {
        log.info("Refunding Paystack payment for transaction: {}", transaction.getTransactionReference());

        if (transaction.getStatus() != PaymentStatus.PAID) {
            throw new PaymentGatewayException("Only successful payments can be refunded");
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("transaction", transaction.getGatewayReference());
            // Convert amount to kobo (multiply by 100) and get int value
            int amountInKobo = transaction.getAmount().multiply(BigDecimal.valueOf(100)).intValue();
            payload.put("amount", amountInKobo);
            payload.put("reason", reason != null ? reason : "Customer requested refund");

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(payload, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    paystackProperties.getBaseUrl() + REFUND_URL,
                    HttpMethod.POST,
                    httpEntity,
                    JsonNode.class
            );

            JsonNode body = response.getBody();
            if (body == null || !body.path("status").asBoolean(false)) {
                String message = body != null ? body.path("message").asText("Unknown error") : "Empty response";
                throw new PaymentGatewayException("Paystack refund failed: " + message);
            }

            transaction.setStatus(PaymentStatus.REFUNDED);
            transaction.setFailureReason("Refunded: " + reason);
            transaction.setGatewayResponse(body.toString());

            log.info("Paystack refund completed for transaction: {}", transaction.getTransactionReference());
            return transaction;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Paystack API error during refund: {}", e.getResponseBodyAsString(), e);
            throw new PaymentGatewayException("Paystack API error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during Paystack refund", e);
            throw new PaymentGatewayException("Failed to refund Paystack payment", e);
        }
    }

    @Override
    public boolean supports(PaymentGateway gateway) {
        return gateway == PaymentGateway.PAYSTACK;
    }

    // ======================== Helpers ========================

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + paystackProperties.getSecretKey());
        return headers;
    }

    private String getCustomerEmail(PaymentTransaction transaction) {
        try {
            return transaction.getOrder().getUser().getEmail();
        } catch (Exception e) {
            log.warn("Could not retrieve customer email, using placeholder", e);
            return "customer@example.com";
        }
    }
}