package com.inkfront.logisticsApplication.domain.jsonb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMetadata {
    private String userAgent;
    private String ipAddress;
    private String deviceId;
    private String sessionId;
    private Map<String, Object> customFields;
    private String orderNumber;
    private String deliveryAddress;
    private String vehicleType;
}