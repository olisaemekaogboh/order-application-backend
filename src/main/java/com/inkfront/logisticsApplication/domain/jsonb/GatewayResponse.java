package com.inkfront.logisticsApplication.domain.jsonb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayResponse {
    private String status;
    private String message;
    private String reference;
    private String authorizationUrl;
    private String accessCode;
    private Map<String, Object> rawResponse;
    private LocalDateTime timestamp;
}