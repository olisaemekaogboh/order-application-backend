package com.inkfront.logisticsApplication.client.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkfront.logisticsApplication.config.properties.PaystackProperties;
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
public class PaystackClient {

    private final PaystackProperties paystackProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String INITIALIZE_URL = "/transaction/initialize";
    private static final String VERIFY_URL = "/transaction/verify/%s";
    private static final String REFUND_URL = "/refund";

    public JsonNode initializePayment(Map<String, Object> payload) {
        return post(INITIALIZE_URL, payload);
    }

    public JsonNode verifyPayment(String gatewayReference) {
        String url = String.format(VERIFY_URL, gatewayReference);
        return get(url);
    }

    public JsonNode refundPayment(Map<String, Object> payload) {
        return post(REFUND_URL, payload);
    }

    private JsonNode post(String path, Map<String, Object> payload) {
        HttpHeaders headers = createHeaders();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    paystackProperties.getBaseUrl() + path,
                    HttpMethod.POST,
                    entity,
                    JsonNode.class
            );
            return validateResponse(response);
        } catch (Exception e) {
            throw new PaymentGatewayException("Paystack API call failed: " + e.getMessage(), e);
        }
    }

    private JsonNode get(String path) {
        HttpHeaders headers = createHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    paystackProperties.getBaseUrl() + path,
                    HttpMethod.GET,
                    entity,
                    JsonNode.class
            );
            return validateResponse(response);
        } catch (Exception e) {
            throw new PaymentGatewayException("Paystack API call failed: " + e.getMessage(), e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + paystackProperties.getSecretKey());
        return headers;
    }

    private JsonNode validateResponse(ResponseEntity<JsonNode> response) {
        JsonNode body = response.getBody();
        if (body == null || !body.path("status").asBoolean(false)) {
            String message = body != null ? body.path("message").asText("Unknown error") : "Empty response";
            throw new PaymentGatewayException("Paystack API error: " + message);
        }
        return body;
    }
}