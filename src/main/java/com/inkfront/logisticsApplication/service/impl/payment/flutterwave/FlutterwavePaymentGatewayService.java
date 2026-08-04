package com.inkfront.logisticsApplication.service.impl.payment.flutterwave;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkfront.logisticsApplication.config.payment.FlutterwaveProperties;
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
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlutterwavePaymentGatewayService implements PaymentGatewayService {

    private final FlutterwaveProperties flutterwaveProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String INITIALIZE_URL = "/payments";
    private static final String VERIFY_URL = "/transactions/%s/verify";
    private static final String REFUND_URL = "/transactions/%s/refund";

    @Override
    public PaymentGateway getGateway() {
        return PaymentGateway.FLUTTERWAVE;
    }

    @Override
    public PaymentTransaction initialize(InitializePaymentRequestDTO request, PaymentTransaction transaction) {
        log.info("Initializing Flutterwave payment for transaction: {}", transaction.getTransactionReference());

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("tx_ref", transaction.getTransactionReference());
            payload.put("amount", transaction.getAmount().doubleValue());
            payload.put("currency", transaction.getCurrency() != null ? transaction.getCurrency() : "NGN");
            payload.put("redirect_url", request.getCallbackUrl() != null ? request.getCallbackUrl() : flutterwaveProperties.getCallbackUrl());
            payload.put("payment_options", request.getPaymentMethod() != null ? request.getPaymentMethod().name() : "card");
            payload.put("meta", request.getMetadata() != null ? request.getMetadata() : Map.of());

            Map<String, Object> customer = new HashMap<>();
            customer.put("email", getCustomerEmail(transaction));
            customer.put("name", getCustomerName(transaction));
            payload.put("customer", customer);

            Map<String, Object> customizations = new HashMap<>();
            customizations.put("title", "Payment for Order " + transaction.getOrder().getOrderNumber());
            payload.put("customizations", customizations);

            log.info("Sending request to Flutterwave: {}", flutterwaveProperties.getBaseUrl() + INITIALIZE_URL);
            log.debug("Request payload: {}", payload);

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(payload, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    flutterwaveProperties.getBaseUrl() + INITIALIZE_URL,
                    HttpMethod.POST,
                    httpEntity,
                    JsonNode.class
            );

            log.info("Flutterwave response status: {}", response.getStatusCode());

            JsonNode body = response.getBody();
            if (body == null) {
                throw new PaymentGatewayException("Flutterwave returned empty response");
            }

            log.info("Flutterwave response: {}", body.toString());

            if (!"success".equals(body.path("status").asText())) {
                String message = body.path("message").asText("Unknown error");
                throw new PaymentGatewayException("Flutterwave initialization failed: " + message);
            }

            JsonNode data = body.path("data");
            transaction.setGatewayReference(data.path("flw_ref").asText());
            transaction.setAuthorizationUrl(data.path("link").asText());
            transaction.setStatus(PaymentStatus.PROCESSING);
            transaction.setGatewayResponse(body.toString());

            log.info("Flutterwave payment initialized successfully: {}", transaction.getTransactionReference());
            log.info("Authorization URL: {}", transaction.getAuthorizationUrl());
            return transaction;

        } catch (ResourceAccessException e) {
            log.error("Connection timeout to Flutterwave API. Please check your internet connection.", e);
            throw new PaymentGatewayException("Connection to Flutterwave timed out. Please try again.", e);
        } catch (HttpClientErrorException e) {
            log.error("Flutterwave HTTP client error: Status={}, Body={}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e);

            // Check for specific error codes
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new PaymentGatewayException("Flutterwave authentication failed. Please check your API keys.");
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new PaymentGatewayException("Invalid request to Flutterwave: " + e.getResponseBodyAsString());
            }
            throw new PaymentGatewayException("Flutterwave API error: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            log.error("Flutterwave server error: {}", e.getResponseBodyAsString(), e);
            throw new PaymentGatewayException("Flutterwave server error. Please try again later.", e);
        } catch (Exception e) {
            log.error("Unexpected error during Flutterwave initialization", e);
            throw new PaymentGatewayException("Failed to initialize Flutterwave payment: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentTransaction verify(PaymentTransaction transaction, String gatewayReference) {
        log.info("Verifying Flutterwave payment for transaction: {}", transaction.getTransactionReference());

        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

            String url = String.format(flutterwaveProperties.getBaseUrl() + VERIFY_URL, gatewayReference);
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    httpEntity,
                    JsonNode.class
            );

            JsonNode body = response.getBody();
            if (body == null || !"success".equals(body.path("status").asText())) {
                String message = body != null ? body.path("message").asText("Unknown error") : "Empty response";
                throw new PaymentGatewayException("Flutterwave verification failed: " + message);
            }

            JsonNode data = body.path("data");
            String status = data.path("status").asText();

            PaymentStatus newStatus;
            switch (status.toLowerCase()) {
                case "successful":
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
                transaction.setFailureReason(data.path("processor_response").asText("Payment failed"));
            }

            log.info("Flutterwave verification completed: {} -> {}", transaction.getTransactionReference(), newStatus);
            return transaction;

        } catch (ResourceAccessException e) {
            log.error("Connection timeout to Flutterwave API during verification", e);
            throw new PaymentGatewayException("Connection to Flutterwave timed out. Please try again.", e);
        } catch (Exception e) {
            log.error("Unexpected error during Flutterwave verification", e);
            throw new PaymentGatewayException("Failed to verify Flutterwave payment", e);
        }
    }

    @Override
    public PaymentTransaction refund(PaymentTransaction transaction, String reason) {
        log.info("Refunding Flutterwave payment for transaction: {}", transaction.getTransactionReference());

        if (transaction.getStatus() != PaymentStatus.PAID) {
            throw new PaymentGatewayException("Only successful payments can be refunded");
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("amount", transaction.getAmount().doubleValue());
            payload.put("currency", transaction.getCurrency() != null ? transaction.getCurrency() : "NGN");
            payload.put("reason", reason != null ? reason : "Customer requested refund");

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(payload, headers);

            String url = String.format(flutterwaveProperties.getBaseUrl() + REFUND_URL, transaction.getGatewayReference());
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpEntity,
                    JsonNode.class
            );

            JsonNode body = response.getBody();
            if (body == null || !"success".equals(body.path("status").asText())) {
                String message = body != null ? body.path("message").asText("Unknown error") : "Empty response";
                throw new PaymentGatewayException("Flutterwave refund failed: " + message);
            }

            transaction.setStatus(PaymentStatus.REFUNDED);
            transaction.setFailureReason("Refunded: " + reason);
            transaction.setGatewayResponse(body.toString());

            log.info("Flutterwave refund completed for transaction: {}", transaction.getTransactionReference());
            return transaction;

        } catch (Exception e) {
            log.error("Unexpected error during Flutterwave refund", e);
            throw new PaymentGatewayException("Failed to refund Flutterwave payment", e);
        }
    }

    @Override
    public boolean supports(PaymentGateway gateway) {
        return gateway == PaymentGateway.FLUTTERWAVE;
    }

    // ======================== Helpers ========================

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String secretKey = flutterwaveProperties.getSecretKey();
        if (secretKey == null || secretKey.isEmpty()) {
            log.error("Flutterwave secret key is not configured!");
            throw new PaymentGatewayException("Flutterwave secret key is missing");
        }
        headers.set("Authorization", "Bearer " + secretKey);
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

    private String getCustomerName(PaymentTransaction transaction) {
        try {
            return transaction.getOrder().getUser().getFullName();
        } catch (Exception e) {
            log.warn("Could not retrieve customer name, using placeholder", e);
            return "Customer";
        }
    }
}