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
public class PaymentData {
    private String cardType;
    private String bank;
    private String channel;
    private String transactionId;
    private Map<String, Object> additionalData;
}