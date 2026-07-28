package com.inkfront.logisticsApplication.client.flutterwave;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkfront.logisticsApplication.config.payment.FlutterwaveProperties;
import com.inkfront.logisticsApplication.exception.PaymentGatewayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlutterwaveClient {

    private final FlutterwaveProperties flutterwaveProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String INITIALIZE_URL = "/payments";
    private static final String VERIFY_URL = "/transactions/%s/verify";
    private static final String REFUND_URL = "/transactions/%s/refund";

    /**
     * Initialize a payment with Flutterwave.
     *
     * @param payload request payload (tx_ref, amount, currency, redirect_url, customer, etc.)
     * @return response JSON from Flutterwave
     */
    public JsonNode initializePayment(Map<String, Object> payload) {
        return post(INITIALIZE_URL, payload);
    }

    /**
     * Verify a payment using the gateway reference (transaction id).
     *
     * @param gatewayReference the Flutterwave transaction ID (flw_ref or transaction id)
     * @return response JSON from Flutterwave
     */
    public JsonNode verifyPayment(String gatewayReference) {
        String url = String.format(VERIFY_URL, gatewayReference);
        return get(url);
    }

    /**
     * Request a refund for a transaction.
     *
     * @param payload refund payload (amount, currency, reason, etc.)
     * @return response JSON from Flutterwave
     */
    public JsonNode refundPayment(Map<String, Object> payload) {
        // Flutterwave refund requires the transaction id in the URL.
        // We expect the payload to contain "transaction_id".
        String transactionId = (String) payload.remove("transaction_id");
        if (transactionId == null) {
            throw new PaymentGatewayException("transaction_id is required for refund");
        }
        String url = String.format(REFUND_URL, transactionId);
        return post(url, payload);
    }

    private JsonNode post(String path, Map<String, Object> payload) {
        HttpHeaders headers = createHeaders();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    flutterwaveProperties.getBaseUrl() + path,
                    HttpMethod.POST,
                    entity,
                    JsonNode.class
            );
            return validateResponse(response);
        } catch (Exception e) {
            throw new PaymentGatewayException("Flutterwave API call failed: " + e.getMessage(), e);
        }
    }

    private JsonNode get(String path) {
        HttpHeaders headers = createHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    flutterwaveProperties.getBaseUrl() + path,
                    HttpMethod.GET,
                    entity,
                    JsonNode.class
            );
            return validateResponse(response);
        } catch (Exception e) {
            throw new PaymentGatewayException("Flutterwave API call failed: " + e.getMessage(), e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + flutterwaveProperties.getSecretKey());
        return headers;
    }

    private JsonNode validateResponse(ResponseEntity<JsonNode> response) {
        JsonNode body = response.getBody();
        if (body == null) {
            throw new PaymentGatewayException("Empty response from Flutterwave");
        }
        // Flutterwave returns status: "success" or "error"
        if (!"success".equals(body.path("status").asText())) {
            String message = body.path("message").asText("Unknown error");
            throw new PaymentGatewayException("Flutterwave API error: " + message);
        }
        return body;
    }
}